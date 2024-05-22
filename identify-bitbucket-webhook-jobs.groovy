import com.atlassian.bitbucket.jenkins.internal.trigger.BitbucketWebhookTriggerImpl;

println 'Listing all Freestyle jobs with a BitBucket Webhook Trigger set... \n'

Jenkins.instance.getAllItems(AbstractProject.class).each{
  if (isTriggerEnabled(it)) {  
    println it.fullName
  }
}

def boolean isTriggerEnabled(ParameterizedJobMixIn.ParameterizedJob job) {
  return job.getTriggers()
  .values()
  .stream()
  .any{v -> v instanceof BitbucketWebhookTriggerImpl};
}

null
