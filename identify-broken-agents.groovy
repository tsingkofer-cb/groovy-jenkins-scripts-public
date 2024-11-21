//User Input
startNumber = 1 //which agent to start with (1 or greater). If you already ran script with batch=100, then this should be 101.
batch = 100 //the total number of agents to sequentially process in this run of the script

count = 0
for (agent in hudson.model.Hudson.instance.slaves) {
  count++
  if(count >= startNumber && count <= startNumber+batch){
    if(agent.getComputer()){
      comp = agent.getComputer()
      if(comp.isOffline()){ //ignore any agents that are already online, we know those are fine.
        comp.connect(false)
        sleep 5000
        if(!comp.isOnline()){ //if the agent is still offline after attempting to connect, print the display name and the agent log.
          println '*** ' + comp.getDisplayName() + ' ***'
          println comp.getOfflineCauseReason()
          println comp.getLog()
        }
        comp.disconnect() //disconnect the agent again to finish up
      }
    }
  }
  if (count == batch+startNumber-1){
  	break
  }
}
println 'Processed agents ' + startNumber + ' through ' + count
