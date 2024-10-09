// define the full name of the folder of which to list all jobs contained within it, and within subfolders.
folderName = 'top-level/subfolder'
println folderName
Jenkins.instance.getItemByFullName(folderName).getAllItems(Job.class).each{
  if(it.getParent().fullName.equals(folderName)){
   println '  ' + it.name
  }
}
Jenkins.instance.getItemByFullName(folderName).getAllItems(com.cloudbees.hudson.plugins.folder.Folder.class).each{
  println it.fullName
  it.getAllItems(Job.class).each{
    println '  ' + it.name
  }
}
null
