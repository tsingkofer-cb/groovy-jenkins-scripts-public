println "Collecting a list of jobs with no build history..."
println ""
baseUrl = Jenkins.instance.getRootUrl()
Jenkins.instance.getAllItems(Job.class).each{
  builds = it.getBuilds()
  if (!builds){
    println baseUrl + it.url
  }
}
return null
