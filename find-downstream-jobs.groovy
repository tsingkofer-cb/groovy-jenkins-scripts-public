// This script will collect a list of all jobs on a controller that were started by a 'Build other remote/local jobs' step, a.k.a the Operations Center Remote Trigger step
// It will also display the URL of the upstream job that called it.
Jenkins.instance.getAllItems(Job.class).each{
    //println it.name + " - " + it.class
    if (it.getLastBuild()) {
      buildCause = it.getLastBuild().getCause(hudson.model.Cause)
      if (buildCause.getClass() == com.cloudbees.opscenter.triggers.RemoteTrigger) {
        //println Jenkins.instance.getRootUrl() + it.url
        println it.url
        //println '    ' + buildCause.getShortDescription() + ' - ' + buildCause.getClass()
        println '    Triggered by: ' + it.getLastBuild().getCause(hudson.model.Cause).getSource().resolveURL()
      }
    }
}
