//When used in a scheduled cluster op, this script can automate the cleanup of queued items that will never run due to agent issues, incorrect labels, etc.

def currentTime = new Date().getTime()
def timeLimitToAbort = 60000 * 20 //20 minutes)

Jenkins.instance.queue.items.each {
    println "URL: $it.task.url\n Queue ID: $it.id\n Assigned Label: $it.assignedLabel\n Time in Queue: $it.inQueueForString\n Cause: $it.causeOfBlockage"
    waitingTime = currentTime-it.getInQueueSince()
  if (waitingTime > timeLimitToAbort) {
    println 'Aborting Build since it has been stuck in the queue for more than the allowed time'
    Jenkins.instance.queue.cancel(it)
    println '---------------------------'
  } else {
    println 'Build has been waiting for ' +  waitingTime/1000/60 + ' minutes. Will not abort.'
    println '---------------------------'
  }
}

return null
