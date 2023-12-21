import com.splunk.splunkjenkins.*;
import com.splunk.splunkjenkins.model.MetaDataConfigItem;
jenkins = Jenkins.getInstance()
splunkExt = jenkins.getExtensionList(SplunkJenkinsInstallation.class)[0]

println 'Configuring Splunk Plugin settings...'

//common configuration section
splunkExt.setEnabled(true)
splunkExt.setHost('')
splunkExt.setPort(8088)
splunkExt.setToken('')
splunkExt.setUseSSL(true)
splunkExt.setGlobalPipelineFilter(true)

//Advanced Section

//example of optional custom metadata config items (from advanced section in UI)
item1 = new MetaDataConfigItem('build_event','index','jenkins_console')
item2 = new MetaDataConfigItem('console_log','index','jenkins_console')
Set<MetaDataConfigItem> metadataConfigItems = new HashSet<>();
metadataConfigItems.add(item1)
metadataConfigItems.add(item2)
splunkExt.setMetadataItemSet(metadataConfigItems)

//splunkExt.setRawEventEnabled(true) //not necessary unless you need to override default
//splunkExt.setIgnoredJobs('^(?:backgroundJobName|adhocJobName|tempJobName)$') //only necessary if you want to ignore some jobs
//splunkExt.setMetadataHost() //Hostname of controller, leave this blank to let the plugin infer it.
//splunkExt.setSplunkAppUrl() //not necessary unless you need to override default it sets
//splunkExt.setMaxEventsBatchSize(262144) //not necessary unless you need to override default
//splunkExt.setRetriesOnError(3) //not necessary unless you need to override default
//splunkExt.setScriptContent() //not necessary unless you need to override default
//splunkExt.setScriptPath() //not necessary unless you need to override default

jenkins.save()

//recommended to restart the controller after executing this script

return null
