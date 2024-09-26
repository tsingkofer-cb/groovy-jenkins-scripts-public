import hudson.slaves.OfflineCause.UserCause

def setAgentsOffline = true //if true, this marks the agent as 'offline' so even if it reconnects, it will not pick up builds. Similar to clicking 'Mark this node temporarily offline' in the UI.
def disconnectAgents = true //if true, this will physically close the connection to the agent, but does not guarantee that it will not reconnect later. Similar to clicking 'Disconnect' in the UI.
def reasonForOffline = 'Offline in preparation for switchaway.' //this will appear in the agent UI as a reason why it is offline

def offlineCause = new UserCause(User.current(),reasonForOffline)
for (agent in hudson.model.Hudson.instance.slaves) {
  if(agent.getComputer()){
    comp = agent.getComputer()
    if(comp.isOnline()){
      println 'Agent: ' + agent.name
      if(disconnectAgents){
        println '  Disconnecting agent'
        comp.disconnect(offlineCause)
      }
      if(setAgentsOffline){
        println '  Marking agent offline' 
        comp.setTemporarilyOffline(true, offlineCause)
      }
    }
  }
}
