import hudson.model.Job
// when false, script will only list jobs using a defined label. When true, jobs will be disabled.
disableJobs = false

debug = false //adds a little extra logging for troubleshooting purposes.

//can search for one or more (comma separated) labels at a time
labels = ['jnlp-agent']

//by default it searches for any of the labels from the list
//set evaluateAnd to true to require all labels much exist in the job
evaluateOr = true

if(labels in String) {
    labels = (labels.contains(',')) ? labels.split(',') : [labels]
}
if(evaluateOr in String) {
    evaluateOr = (evaluateOr != 'false')
}

//type check user defined parameters/bindings
if(!(labels in List) || (false in labels.collect { it in String } )) {
    throw new Exception('PARAMETER ERROR: labels must be a list of strings.')
}
if(!(evaluateOr in Boolean)) {
    throw new Exception('PARAMETER ERROR: evaluateOr must be a boolean.')
}

projects = [] as Set
//getAllItems searches a global lookup table of items regardless of folder structure
Jenkins.instance.getAllItems(Job.class).each { Job job ->
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
      return
    }
    List results = labels.collect { label ->
      if(jobLabelString){  
        jobLabelString.contains(label)
      }
    }

    if(evaluateOr) {
        //evaluate if any of the labels exist in job
        labelFound = true in results
    } else {
        //evaluate requiring all labels to exist in job
        labelFound = !(false in results)
    }

    if(labelFound) {
        projects << job.fullName
        if (disableJobs){
          job.disabled = true
          job.save()
        }
    }
}
println '**Jobs using targetted label(s):**'
println(projects.join('\n'))
if (disableJobs){
  println ''
  println 'All identified jobs have been disabled.'
}

//null so no result shows up
null
