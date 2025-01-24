import com.cloudbees.pipeline.policy.PoliciesApplied

Jenkins.instance.getAllItems(Job.class).each{
  build = it.getLastSuccessfulBuild()
  if(build){
    vioCount = 0
    policiesApplied = build.getActions(PoliciesApplied)[0]
    if(policiesApplied){
      vioCount = policiesApplied.getViolationCount()
    }
    if (vioCount > 0) {
        println it.getAbsoluteUrl() + " has " + vioCount + " policy violations."
    }
  }
}
null
