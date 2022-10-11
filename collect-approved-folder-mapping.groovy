import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.jenkins.plugins.foldersplus.SecurityGrantsFolderProperty;

def folderItems = Jenkins.instance.getAllItems(Folder.class)

//CSV Column Headings
println "Agent,Folder"

for (folderItem in folderItems) {
	//println "Folder : " + folderItem.name

  SecurityGrantsFolderProperty securityProperty = SecurityGrantsFolderProperty.of(folderItem)
  if (securityProperty != null) {
    securityProperty.getSecurityGrants().each {
      //For more readable script output instead of csv
      //println "  SecurityGrant : "
      //println "    Folder: " + it.getFolder().name
      //println "    Node: " + it.getNode().name
      
      //For CSV format:
      println it.getNode().name + ',' + it.getFolder().name
    }
  }
}
