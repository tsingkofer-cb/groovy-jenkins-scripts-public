import com.cloudbees.hudson.plugins.folder.AbstractFolder
import nectar.plugins.rbac.groups.GroupContainer
import nectar.plugins.rbac.groups.GroupContainerLocator

// Set the valid group name prefix to search for
def groupPrefix = 'jenkins-'
def userPrefix = 'svc'
// When dryRun = true, no actual changes will be made to the system, but you will see simulated output.
def dryRun = true

Jenkins.instance.getAllItems(AbstractFolder.class).each{
  //Search through each folder and collect the RBAC data for folder groups
  print "Folder: " + it.fullName + "\n"
  GroupContainer container = GroupContainerLocator.locate(it);
  findAllGroups(container, groupPrefix, userPrefix, dryRun)
}

def findAllGroups(GroupContainer fpgc, String groupPrefix, String userPrefix, Boolean dryRun) {
  if (fpgc != null) {
    fpgc.getGroups().findAll { it != null }.each {
      println "  Group: " + it.name
      
      //Check configured User entries
      def groupUsers = it.getUsers()
      def usersToRemove = []
      for (user in groupUsers) { 
        println '    User: ' + user
        if (!user.startsWith(groupPrefix) && !user.startsWith(userPrefix)){
          println '      Adding invalidly named user entry to delete list...' 
          usersToRemove.add(user)
        }
      }
      if (usersToRemove && !dryRun){
        for (removeUser in usersToRemove) {
          groupUsers.remove(removeUser)
        }
        println '    removing group entries...'
        it.setUsers(groupUsers)
        it.save()
      } else if (usersToRemove && dryRun) {
        println '    Dry Run enabled, skipping actual removal step.'
      }

      //Check configured Group entries
      def groupGroups = it.getGroups()
      def groupsToRemove = []
      for (group in groupGroups) {
        println '    Group: ' + group
        if (!group.startsWith(groupPrefix) && !group.startsWith(userPrefix)){
          println '      Adding invalidly named group entry to delete list...' 
	      groupsToRemove.add(group)
        }
      }
      if (groupsToRemove && !dryRun){
        for (removeGroup in groupsToRemove) {
          groupGroups.remove(removeGroup)
        }
        println '    removing group entries...'
        it.setGroups(groupGroups)
        it.save()
      } else if (groupsToRemove && dryRun) {
        println '    Dry Run enabled, skipping actual removal step.'
      }

      //Check configured Member (ambiguous) entries
      def groupMembers = it.getMembers()
      def membersToRemove = []
      def membersToConvertToUsers = []
      def membersToConvertToGroups = []
      for (member in groupMembers) { 
        println '    Ambiguous Member: ' + member 
        if (!member.startsWith(groupPrefix) && !member.startsWith(userPrefix)){
          println '      Adding invalidly named group entry to delete list...' 
	      membersToRemove.add(member)
        } else if (member.startsWith(groupPrefix)){
          println '    Valid group name, converting to Group RBAC Object...'
          membersToRemove.add(member)
          membersToConvertToGroups.add(member)
        }
        else if (member.startsWith(userPrefix)){
          println '    Valid service account name, converting to User RBAC Object...'
          membersToRemove.add(member)
          membersToConvertToUsers.add(member)
        }
      }
      if (!dryRun){
        if(membersToRemove){
          for (removeMember in membersToRemove) {
            groupMembers.remove(removeMember)
          }
          println '    removing member entries...'
          it.setMembers(groupMembers)
          it.save()
        }
        if(membersToConvertToGroups){
          println '    converting valid group entries...'
          //get current group entries
          def currentGroups = it.getGroups()
          //add convert entries
          currentGroups.addAll(membersToConvertToGroups)
          //set to save
          it.setGroups(currentGroups)
          it.save()
        }
        if(membersToConvertToUsers){
          println '    converting valid service account entries...'
          //get current user entries
          def currentUsers = it.getUsers()
          //add convert entries
          currentUsers.addAll(membersToConvertToUsers)
          //set to save
          it.setUsers(currentUsers)
          it.save()
        }
      } else if ((membersToRemove || membersToConvertToGroups || membersToConvertToUsers) && dryRun) {
        println '    Dry Run enabled, skipping actual removal step.'
      }
    }
  }
}

def findAllItems(items){
  for(item in items)
  {
    if (item instanceof AbstractFolder) {
      GroupContainer container = GroupContainerLocator.locate(item);
      println "Folder: " + item.name
      findAllGroups(container)
      //Drill into folders
      findAllItems(((AbstractFolder) item).getItems())
    }
  }
}
return
