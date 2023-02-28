def plugin = Jenkins.get().getPlugin('maven-plugin')
if (plugin != null) {
  println 'maven-plugin installed, used by the following jobs:'
  Jenkins.get().getAllItems(hudson.maven.AbstractMavenProject.class).each { println it.fullName }
} else {
  println 'maven-plugin not installed'
}
return null
