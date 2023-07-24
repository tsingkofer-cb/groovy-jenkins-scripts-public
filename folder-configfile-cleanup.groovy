// Until https://issues.jenkins.io/browse/JENKINS-71414 adds CasC compatibility for folder-level config files, item exports are brokens for any folders 
// where a managed config file is currently or previously has been created.

// This script will find those folders where there are no longer config files present, and cleanup the unused property entries so that CasC exports will work.

// Folders that actually contain a managed file will still break on attempted export.

def dryRun = true
def folders = Jenkins.instance.getAllItems(com.cloudbees.hudson.plugins.folder.AbstractFolder.class)

for (folderItem in folders){
  def properties = folderItem.getProperties()
  for (prop in properties){
    if (prop instanceof org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty ){
      println folderItem.name
      def configFileEntries = folderItem.getProperties().get(org.jenkinsci.plugins.configfiles.folder.FolderConfigFileProperty.class).getConfigs()
      println '  Config File entries: ' + configFileEntries
      if (!configFileEntries){
        if (!dryRun){
          println '  empty config file property block found...cleaning that up.'
          properties.remove(prop.descriptor)
        } else {
          println '  empty config file property block found...but skipping cleanup since dryRun=true'
        }
      }
    }
    }
}
