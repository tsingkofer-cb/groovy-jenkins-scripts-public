#Run this commented out script in the CJOC script console to collect the domain (name) of each controller to use for it's JENKINS_URL
#Copy/paste the output inside the quotes for the 'list' variable in this shell script.

# def mms = Jenkins.instance.getAllItems(com.cloudbees.opscenter.server.model.ManagedMaster)
# mms.each {
#   print it.getConfiguration().domain + ' '
# }
# null

hostname=""
list=""
for controller in $list
do
   echo ${controller}
   curl -XPOST -u $JENKINS_USER_ID:$JENKINS_API_TOKEN "https://${hostname}/${controller}/manage/administrativeMonitor/OldData/discard"
done
