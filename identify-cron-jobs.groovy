import hudson.triggers.TimerTrigger
import com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger 

println 'Listing all items with a timer set...'

Jenkins.instance.getAllItems().each{
  if(it.hasProperty('triggers')){
    if (it.triggers.values()) { 
      timerExists = it.triggers.values().any{ t -> t.getClass() == TimerTrigger || t.getClass() == PeriodicFolderTrigger }
      if (timerExists){
      	println ''
        println it.absoluteUrl
        println it.getClass()
      	for (trigger in it.triggers.values()) {
        	println trigger.spec
        	if (trigger.hasProperty('interval')){
	        	println trigger.interval 
        	}
      	}
      }
    }
  }
}
  
null
