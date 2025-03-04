def dryRun = true

for (job in Jenkins.instance.getAllItems(Job.class)) {
  if (job.getClass() != hudson.model.ExternalJob) {
    println job.fullName
    if (!job.disabled) {
      println '  Disabling job.'
      if (!dryRun){
        job.disabled = true
        job.save()
      }
    } else {
   	  println '  Already disabled.'
    }
  }
}
