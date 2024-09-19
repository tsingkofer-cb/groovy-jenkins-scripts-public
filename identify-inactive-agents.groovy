//User Input
def days = 365 //number of days without a build for an agent to be considered inactive
def printExplaination = true //if false, will only print out the agent name without the reason.

// There are two scenarios this script uses to consider and agent as "inactive"
// 1. No build history 
// 2. No builds run in the last x days.

def cutOffTime = System.currentTimeMillis() - 1000L * 60 * 60 * 24 * days

for (agent in hudson.model.Hudson.instance.slaves) {
  if(agent.getComputer()){
    comp = agent.getComputer()
    if(!comp.getBuilds()){
      println agent.name
      if (printExplaination) {
        println "  No build history found for this agent."
        println "  Last offline cause was '" + comp.getOfflineCauseReason() + "'"
        println "  This occured at: " + comp.getOfflineCause().getTime()
      }
    } else {
      lastBuild = comp.getBuilds().getLastBuild()
      if (lastBuild.getTimeInMillis() < cutOffTime){
        println agent.name
        if (printExplaination){
          println "  This agent has not run a build in more than " + days + " days."
          println "  Last build on: " + lastBuild.getTime()
        }
      }
    }
  }
}
