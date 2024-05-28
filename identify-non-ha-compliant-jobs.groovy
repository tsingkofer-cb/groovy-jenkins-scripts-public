import com.cloudbees.jenkins.plugins.replication.ui.PipelineDurabilityAdministrativeMonitor;

//  List all jobs that do not have the durabilityHint set to MAX_SURVIVABILITY and/or have 
// 'Do not allow the pipeline to resume if the controller restarts', a.k.a disableResume() enabled.

for (job in Jenkins.instance.getAllItems(Job.class)) {
  if (PipelineDurabilityAdministrativeMonitor.checkDurabilityHint(job) || PipelineDurabilityAdministrativeMonitor.checkDisableResumeJobProperty(job)) {
    println job.fullName
  }
}
