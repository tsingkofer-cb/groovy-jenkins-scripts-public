# First, run this commented out script in the CJOC script console to collect the domain (name) of each controller to use to construct it's URL
# Copy/paste the output inside the quotes for the 'list' variable in this shell script. It should be a space-separated list of controller names, all on a single line.

# def mms = Jenkins.instance.getAllItems(com.cloudbees.opscenter.server.model.ManagedMaster)
# mms.each {
#   print it.getConfiguration().domain + ' '
# }
# null

############################################

# Make sure you place this script, the jenkins-cli.jar, and the user-activity-monitoring-1.50.hpi file all in the same folder, and then execute the script from within that folder. 
# the -restart flag can be removed from the CLI command below if you prefer not to restart the controllers to complete the plugin upgrade immediately and prefer to do that later
hostname=""
list=""
for controller in $list
do
   echo ${controller}
   java -jar jenkins-cli.jar -auth $JENKINS_USER_ID:$JENKINS_API_TOKEN -webSocket -s https://$hostname/$controller install-plugin -restart = < user-activity-monitoring-1.50.hpi
done
