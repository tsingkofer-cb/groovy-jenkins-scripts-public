def plugin = Jenkins.get().getPlugin('maven-plugin')
if (plugin != null) {
  println 'maven-plugin installed, used by the following jobs:'
  Jenkins.get().getAllItems(hudson.maven.AbstractMavenProject.class).each { 
    def lastBuild = it.getLastBuild()
    if (lastBuild) {
	    println it.fullName + ' - last built on: ' + it.getLastBuild().getTimestampString2() 
    } else {
      println it.fullName + ' - contains no build records, likely was never run.'
    }
  }
} else {
  println 'maven-plugin not installed'
}
return null
