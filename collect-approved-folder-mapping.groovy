import com.cloudbees.hudson.plugins.folder.Folder
import com.cloudbees.jenkins.plugins.foldersplus.SecurityGrantsFolderProperty;

def outputCsv = true //set to false for more readable script output, true for csv format

def folderItems = Jenkins.instance.getAllItems(Folder.class)

//CSV Column Headings
if (outputCsv){
  println "Agent,Folder"
}
for (folderItem in folderItems) {
  SecurityGrantsFolderProperty securityProperty = SecurityGrantsFolderProperty.of(folderItem)
  if (securityProperty != null) {
    securityProperty.getSecurityGrants().each {
      //For more readable script output instead of csv
      if(!outputCsv){
        println "  SecurityGrant : "
        println "    Folder: " + it.getFolder().fullName
        println "    Node: " + it.getNode().name
      }
      if(outputCsv){
        println it.getNode().name + ',' + it.getFolder().fullName
      }
    }
  }
}
