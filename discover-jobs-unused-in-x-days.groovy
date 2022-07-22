import hudson.model.Job
import jenkins.model.Jenkins

def format = 'csv' //output format can be 'text' or 'csv'
def days = 365
def cutOffTime = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * days
def cutOffDate = new Date(cutOffTime).format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

println "Finding jobs that have not been built since ${cutOffDate} (more than ${days} days ago)..."

if (format == 'csv') {
  println '' 
  println 'Job URL,Last Run'
}
for (job in Jenkins.instance.getAllItems(Job.class)) {
    def build = job.getLastSuccessfulBuild()

    if (build != null && build.getTimeInMillis() < cutOffTime) {
      if (format == 'text'){
        println ""
        println "Job URL     : " + job.absoluteUrl
        println "Build Number: " + build.number
        println "Job Last Run: " + build.timestampString2
      } else if (format == 'csv') {
        println job.absoluteUrl + ',' + build.timestampString2
      } else {
        throw new Exception("Output format option must be text or csv.")
      }
    }
}
