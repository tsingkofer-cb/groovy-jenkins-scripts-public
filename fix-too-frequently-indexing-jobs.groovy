import hudson.triggers.TimerTrigger
import com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger 

dryRun = true

// see list below for the Display Interval to Milliseconds mapping
maxIntervalString = '1d'
maxIntervalMillis = 86400000
// 1h : 3600000
// 2h : 7200000
// 4h : 14400000
// 8h : 28800000
// 12h : 43200000
// 1d : 86400000
// 2d : 172800000
// 1w : 604800000
// 2w : 1209600000
// 4w : 2419200000

println 'Listing all items with a timer set to run more frequently than allowed...'
if (!dryRun){
  println 'Will also update any out of compliance MBP or Organization jobs to set the index interval to ' + maxIntervalString
}

Jenkins.instance.getAllItems().each{
  if(it.hasProperty('triggers')){
    if (it.triggers.values()) { 
      timerExists = it.triggers.values().any{ t -> t.getClass() == TimerTrigger || t.getClass() == PeriodicFolderTrigger }
      if (timerExists){
      	for (trigger in it.triggers.values()) {
        	if (trigger.hasProperty('interval')){
              if (trigger.getIntervalMillis() < maxIntervalMillis ){
                println ''
                println it.absoluteUrl
                println it.getClass()
                println 'Current indexing interval: ' + trigger.interval
                println '** Indexing interval is too frequent. **'
                if (!dryRun){
                  println '** Updating to max allowed interval: ' + maxIntervalString + ' **'
                  //update to max interval
                  it.addTrigger(new PeriodicFolderTrigger(maxIntervalString))
                  it.save()
                }
              }
            } else {
              if (trigger.tabs.next() && trigger.tabs.previous()){
                calculatedInterval = trigger.tabs.next().getTimeInMillis()-trigger.tabs.previous().getTimeInMillis()
                if (calculatedInterval < maxIntervalMillis){          
                  println ''
                  println it.absoluteUrl
                  println it.getClass()
                  println trigger.spec
                  println '** This job seems to be configured to run more frequently than the max allowed interval. Please review. **' 
                  println '   Calculated Interval: ' + calculatedInterval + 'ms, or ' + calculatedInterval/1000/60/60 + ' hours.'
                }
              }
            }
      	}
      }
    }
  }
}
  
null
