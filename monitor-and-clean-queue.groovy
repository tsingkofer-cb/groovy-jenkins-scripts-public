//When used in a scheduled cluster op, this script can automate the cleanup of queued items that will never run due to agent issues, incorrect labels, etc.

def currentTime = new Date().getTime()
def timeLimitToAbort = 60000 * 20 //20 minutes)

Jenkins.instance.queue.items.each {
    println "Queue ID: $it.id, Assigned Label: $it.assignedLabel, Time in Queue: $it.inQueueForString, Cause: $it.causeOfBlockage"
    waitingTime = currentTime-it.getInQueueSince()
  if (waitingTime > timeLimitToAbort) {
    println 'Aborting Build since it has been stuck in the queue for more than the allowed time'
    Jenkins.instance.queue.cancel(it)
  } else {
    println 'Build has been waiting for ' +  waitingTime/1000/60 + ' minutes. Will not abort.'
  }
}

return null
