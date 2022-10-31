import hudson.slaves.ComputerLauncher
import com.cloudbees.jenkins.plugins.sshslaves.SSHConnectionDetails

def newPrefixStartSlaveCmd = '''powershell -Command "cd E:\\jarfolder ; E:\\path\\to\\bin\\java -jar agent.jar" ; exit 0 ; # ' '''

//Get all agent nodes for this controller
def nodes = Jenkins.instance.getNodes()

// For each agent that connects over SSH and already has 'powershell' in the prefix start command field, print the agent name and update that field with the value in newPrefixStartSlaveCmd
println 'Updating Prefix Start Command for the following agents: '
for (node in nodes) {
  if (node.getLauncher().getClass().toString().equals('class com.cloudbees.jenkins.plugins.sshslaves.SSHLauncher')){
    if (node.getLauncher().getPrefixStartSlaveCmd().contains('powershell')) {
      println node.getNodeName()
      def launcher = node.getLauncher()
      def connectionDetails = launcher.getConnectionDetails()
      ComputerLauncher updatedLauncher = new com.cloudbees.jenkins.plugins.sshslaves.SSHLauncher(
        launcher.getHost(), // Host
        new SSHConnectionDetails(
                connectionDetails.credentialsId, 
                connectionDetails.port, 
                connectionDetails.javaPath, 
                connectionDetails.jvmOptions, 
                newPrefixStartSlaveCmd, 
                connectionDetails.suffixStartSlaveCmd, 
                connectionDetails.displayEnvironment, // Log environment on initial connect
                connectionDetails.keyVerificationStrategy // Host Key Verification Strategy
        ))
      node.setLauncher(updatedLauncher)
      Jenkins.instance.updateNode(node)
    }
  }
}
