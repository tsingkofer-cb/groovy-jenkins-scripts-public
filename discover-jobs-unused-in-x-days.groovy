import hudson.model.Job
import jenkins.model.Jenkins

def format = 'text' //output format can be 'text' or 'csv'
def days = 365
def cutOffTime = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * days
def cutOffDate = new Date(cutOffTime).format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
def disableUnusedJobs = false

if (disableUnusedJobs){
  println "Disabling jobs that have not been built since ${cutOffDate} (more than ${days} days ago)..."
  println ""
} else {
  println "Finding jobs that have not been built since ${cutOffDate} (more than ${days} days ago)..."
  println ""
}
if (format == 'csv') {
  println '' 
  println 'Job URL,Last Run'
}
for (job in Jenkins.instance.getAllItems(Job.class)) {
    def build = job.getLastSuccessfulBuild()

    if (build != null && build.getTimeInMillis() < cutOffTime) {
      if (disableUnusedJobs){
        job.disabled = true
        job.save()
      }
      if (format == 'text'){
        println "Job URL     : " + job.absoluteUrl
        println "Build Number: " + build.number
        println "Job Last Run: " + build.timestampString2
        println ""
      } else if (format == 'csv') {
        println job.absoluteUrl + ',' + build.timestampString2
      } else {
        throw new Exception("Output format option must be text or csv.")
      }
    }
}
