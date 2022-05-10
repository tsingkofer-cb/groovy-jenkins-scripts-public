println 'Listing all Maven Integration Plugin jobs on this instance...'
Jenkins.instance.getAllItems(Job).each{job -> job.isBuildable()

if (job.isBuildable()!=null && ( job.class.toString() == 'class hudson.maven.MavenModuleSet' || job.class.toString() == 'class hudson.maven.MavenModule' ) )
	println job.fullName
}

return null
