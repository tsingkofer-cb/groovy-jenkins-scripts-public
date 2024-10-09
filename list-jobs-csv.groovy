//list all jobs with the folder they are contained within in a CSV format
Jenkins.instance.getAllItems(Job.class).each{
  parentName = it.getParent().fullName
  if(!parentName){
    parentName = 'root'
  }
  println parentName + ',' + it.name
}
null
