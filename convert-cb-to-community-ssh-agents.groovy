/*
* This script assumes that both the CloudBees and Community SSH Build Agent plugins are already installed.
* https://docs.cloudbees.com/docs/release-notes/latest/plugins/cloudbees-ssh-build-agents-plugin/
* https://plugins.jenkins.io/ssh-slaves/
* Running this on a controller with convert any existing SSH agents configured with the CloudBees SSH launcher (Non-Blocking I/O) 
* to use the SSH launcher provided by the community plugin. 
* For example, to resolve the issue: CloudBees SSH build agents disconnect on rekey events (BEE-18090)
*/
import hudson.slaves.ComputerLauncher
import com.cloudbees.jenkins.plugins.sshslaves.SSHConnectionDetails
import hudson.plugins.sshslaves.verifiers.*

def dryRun = true

if (dryRun){
  println '*** executing in simulation mode, change dryRun value to false to actually apply changes ***'
  println ''
}

//Get all agent nodes for this controller
def nodes = Jenkins.instance.getNodes()

// For each agent that connects over SSH using the CloudBees plugin, recreate that launcher using the community plugin launcher
println 'Converting to the Community SSH Build Agents plugin for the following agents: '
for (node in nodes) {
  if (node.getLauncher().getClass().toString().equals('class com.cloudbees.jenkins.plugins.sshslaves.SSHLauncher')){
      println node.getNodeName() + ' : ' + node.getLauncher().getClass().toString()
      def launcher = node.getLauncher()
      def connectionDetails = launcher.getConnectionDetails()
      def ossConnectionStrategy = ''
      println '    Current Host Key Verification strategy: ' + connectionDetails.keyVerificationStrategy.getClass().getSimpleName().toString()
      switch (connectionDetails.keyVerificationStrategy.getClass().toString()){
        case "class com.cloudbees.jenkins.plugins.sshslaves.verification.TrustInitialConnectionVerificationStrategy":
          ossConnectionStrategy = new ManuallyTrustedKeyVerificationStrategy(connectionDetails.keyVerificationStrategy.isManualVerification())
          break
        case "class com.cloudbees.jenkins.plugins.sshslaves.verification.ManuallyConnectionVerificationStrategy":
          ossConnectionStrategy = new ManuallyProvidedKeyVerificationStrategy(connectionDetails.keyVerificationStrategy.getKey())
          break
        case "class com.cloudbees.jenkins.plugins.sshslaves.verification.KnownHostsConnectionVerificationStrategy":
          ossConnectionStrategy = new KnownHostsFileKeyVerificationStrategy()
          break
        case "class com.cloudbees.jenkins.plugins.sshslaves.verification.BlindTrustConnectionVerificationStrategy":
          ossConnectionStrategy = new NonVerifyingKeyVerificationStrategy()
          break
      }
      ComputerLauncher updatedLauncher = new hudson.plugins.sshslaves.SSHLauncher(
        launcher.getHost(),
        connectionDetails.port,
        connectionDetails.credentialsId,
        connectionDetails.jvmOptions,
        connectionDetails.javaPath,
        connectionDetails.prefixStartSlaveCmd, 
        connectionDetails.suffixStartSlaveCmd,
        60, //connection timeout in seconds (default)
        10, //max number of retries (default)
        15, //seconds to wait between retries (default)
        ossConnectionStrategy
      )
      if (!dryRun){
        println '    Applying changes...'
        node.setLauncher(updatedLauncher)
        Jenkins.instance.updateNode(node)
      }
  }
}
