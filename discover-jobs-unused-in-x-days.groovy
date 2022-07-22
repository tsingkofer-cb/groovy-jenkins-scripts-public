import hudson.model.Job
import jenkins.model.Jenkins

def days = 365
def cutOffTime = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * days
def cutOffDate = new Date(cutOffTime).format("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

println "Finding jobs that have not been built since ${cutOffDate} (more than ${days} days ago)..."
for (job in Jenkins.instance.getAllItems(Job.class)) {

    def build = job.getLastSuccessfulBuild()

    if (build != null && build.getTimeInMillis() < cutOffTime) {
        println ""
        println "Job URL     : " + job.absoluteUrl
        println "Build Number: " + build.number
        println "Job Last Run: " + build.timestampString2
    }
}
