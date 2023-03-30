println 'Listing all jobs with a cron timer set... \n'

Jenkins.instance.getAllItems(Job.class).each{
  if (it.triggers.values()) {  
    println it.fullName
    for (trigger in it.triggers.values()) {
      println trigger.spec 
    }
    println ''
  }
}
  
null
