for (agent in hudson.model.Hudson.instance.slaves) {
  if(agent.getComputer()){
    comp = agent.getComputer()
    if(comp.isOffline()){ //ignore any agents that are already online, we know those are fine.
      comp.connect(false)
      sleep 5000
      if(!comp.isOnline()){ //if the agent is still offline after attempting to connect, print the display name and the agent log.
        println '*** ' + comp.getDisplayName() + ' ***'
        println comp.getLog()
        println comp.getOfflineCauseReason()
      }
      comp.disconnect() //disconnect the agent again to finish up
    }
  }
}
