import hudson.model.*
import jenkins.model.*
import hudson.slaves.*
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry
import hudson.tools.ToolLocationNodeProperty
import hudson.tools.ToolLocationNodeProperty.ToolLocation
import hudson.plugins.sshslaves.verifiers.*
import com.cloudbees.jenkins.plugins.replication.builds.ReplicatedRetentionStrategy;

//Get all agent nodes for this controller
def nodes = Jenkins.instance.getNodes()

def dryRun = true

if (dryRun){
  println '*** executing in simulation mode, change dryRun value to false to actually apply changes ***'
  println ''
}

for (node in nodes) {
  println node.getNodeName() + ' :\n  # of Executors: ' + node.getNumExecutors() + '\n  Root Directory: ' + node.getRemoteFS()
  if (node.getLauncher().getClass().toString().equals('class hudson.plugins.sshslaves.SSHLauncher')){
    if(node.getNumExecutors() > 1){
        println '  ' + node.getNodeName() + ' currently configured with ' + node.getNumExecutors() + ' executors. Setting to 1 and splitting into ' + node.getNumExecutors() + ' separate nodes.'
        if(!dryRun){
        //Create new nodes
        for( i = 1 ; i < node.getNumExecutors() ; i++ ) {
            def launcher = node.getLauncher()         
            ComputerLauncher newLauncher = new hudson.plugins.sshslaves.SSHLauncher(
                launcher.getHost(),
                launcher.getPort(),
                launcher.getCredentialsId(),
                launcher.getJvmOptions(),
                launcher.getJavaPath(),
                launcher.getPrefixStartSlaveCmd(), 
                launcher.getSuffixStartSlaveCmd(),
                60, //connection timeout in seconds (default)
                10, //max number of retries (default)
                15, //seconds to wait between retries (default)
                launcher.getSshHostKeyVerificationStrategy()
            )

            // Define a "Permanent Agent"
            Slave agent = new DumbSlave(
                node.getNodeName()+'-'+i,
                node.getRemoteFS()+'-'+i,
                newLauncher)
            agent.nodeDescription = node.getNodeDescription()
            agent.numExecutors = 1
            agent.labelString = node.getLabelString()
            agent.mode = node.getMode()
            agent.retentionStrategy = new ReplicatedRetentionStrategy()
            
            List<Entry> env = new ArrayList<Entry>();
            List<ToolLocation> tools = new ArrayList<ToolLocation>();
            nodeProps = node.getNodeProperties()
            for (prop in nodeProps){
              if(prop.getClass().toString().equals('class hudson.slaves.EnvironmentVariablesNodeProperty')){
                for (p in prop.getEnv()){
                  key = p.key.toString()
                  value = p.value.toString()
                  env.add(new Entry(key,value))
                }
              }
              if(prop.getClass().toString().equals('class hudson.tools.ToolLocationNodeProperty')){
                for (p in prop.getLocations()){
                  key = p.getKey()
                  home = p.getHome()
                  tools.add(new ToolLocation(key,home))
                }
              }
            }
            EnvironmentVariablesNodeProperty envPro = new EnvironmentVariablesNodeProperty(env);
            agent.getNodeProperties().add(envPro)
            ToolLocationNodeProperty toolLocations = new ToolLocationNodeProperty(tools)
            agent.getNodeProperties().add(toolLocations)

            // Create a "Permanent Agent"
            Jenkins.instance.addNode(agent)

            println '    Node ' + node.getNodeName()+'-'+i + ' has been created successfully.\n'
        }
        //Update # of executors and availability settings on original agent
        node.setNumExecutors(1)
        node.setRetentionStrategy(new ReplicatedRetentionStrategy())
        Jenkins.instance.updateNode(node)
        }
    } else if (!node.getRetentionStrategy().getClass().toString().equals('class com.cloudbees.jenkins.plugins.replication.builds.ReplicatedRetentionStrategy')){
        println '  ' + node.getNodeName() + ' already set to 1 executor, but not the correct Availability setting for HA.' 
        if(!dryRun){
          println '  Changing Availability setting to CloudBees High Availability...'
          //Update availability settings on agent
          node.setRetentionStrategy(new ReplicatedRetentionStrategy())
          Jenkins.instance.updateNode(node)
        }
        println ''
    }
  } else {
    println '  Skipping agent since this script only supports agents using the SSH Build Agents plugin.\n'
  }
}

