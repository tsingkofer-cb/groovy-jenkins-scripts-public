import com.cloudbees.jenkins.plugins.replication.builds.MultipleExecutorsProperty;
import jenkins.model.NodeListener;

nodes = Jenkins.instance.getNodes()
for (node in nodes){
  prop = node.getNodeProperty(com.cloudbees.jenkins.plugins.replication.builds.MultipleExecutorsProperty)
  if (prop) {
    println node.name
    numExec = prop.numExecutors
    println "  Current # of HA exec: " + numExec
    
    //Remove HA executor property and set the regular number of executors to that int
    node.getNodeProperties().remove(prop.getDescriptor())
    node.setNumExecutors(numExec)
    Jenkins.get().updateNode(node)
    nls = NodeListener.all()
    //this is what forces the controller to review the current state of the agent config, and clean up the clones
    for (listener in nls){
      if (listener.getClass().toString().equals('class com.cloudbees.jenkins.plugins.replication.builds.MultipleExecutorsProperty$ConfigListener')){
        listener.enforce(node)
      }
    }
    
    //It might be good to add some delay to make sure the agent clones are completely gone before re-adding
    // this is in ms
    sleep 5000     
    
    //Reapply the HA executor setting from before
    node.getNodeProperties().add(new MultipleExecutorsProperty(numExec))
    node.setNumExecutors(1)
    Jenkins.get().updateNode(node)
    //this is what forces the controller to review the current state of the agent config, and re-create up the agent clones
    for (listener in nls){
      if (listener.getClass().toString().equals('class com.cloudbees.jenkins.plugins.replication.builds.MultipleExecutorsProperty$ConfigListener')){
        listener.enforce(node)
      }
    }
  }
}
