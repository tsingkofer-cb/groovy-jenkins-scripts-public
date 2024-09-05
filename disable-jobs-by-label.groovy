//This script will search for Freestyle and Pipeline jobs only to identify and optionally disable jobs using a given label.

import hudson.model.Job

//User Inputs/Settings
disableJobs = false // when false, script will only list jobs using a defined label. When true, jobs will be disabled.
debug = false //adds a little extra logging for troubleshooting purposes.
labels = ['label1','label2'] //can search for one or more (comma separated) labels at a time

if(labels in String) {
    labels = (labels.contains(',')) ? labels.split(',') : [labels]
}

println 'Searching for enabled jobs containing ANY of the following labels:'
labels.each { String label ->
  println '  ' + label
}

//type check user defined parameters/bindings
if(!(labels in List) || (false in labels.collect { it in String } )) {
    throw new Exception('PARAMETER ERROR: labels must be a list of strings.')
}

jobCount = 0
projects = [] as Set
//getAllItems searches a global lookup table of items regardless of folder structure
List enabledJobs = Jenkins.instance.getAllItems(Job.class).findAll { Job item ->
  item.hasProperty('disabled') && !item.disabled }
for (job in enabledJobs) {
    Boolean labelFound = false
    String jobLabelString
    if(debug){  
      println job.fullName + ' ' + job.class
    }
    if(job.class.simpleName == 'FreeStyleProject') {  
        jobLabelString = job.getAssignedLabelString()
    } else if(job.class.simpleName == 'WorkflowJob') {
      try { 
	    jobLabelString = job.getDefinition().getScript()
      } catch (Exception e) {
          try {
            jobLabelString = job.getDefinition().getScriptPath()
          } catch (Exception ex){
            if(debug){  
              println '  *no script to find'
            }
          }
      }
    } else {
      continue
    }
    List results = labels.collect { label ->
      if(jobLabelString){  
        jobLabelString.contains(label)
      }
    }

    labelFound = true in results
    if(labelFound) {
        projects << job.getAbsoluteUrl()
        jobCount++
        if (disableJobs){
          job.disabled = true
          job.save()
        }
    } 
}
println '**** ' + jobCount + ' jobs using targetted label(s): ****'
println(projects.join('\n'))
if (disableJobs){
  println ''
  println '**** All identified jobs have been disabled. ****'
}

//null so no result shows up
null
