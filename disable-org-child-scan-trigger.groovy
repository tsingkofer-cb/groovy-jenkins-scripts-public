import jenkins.branch.OrganizationChildTriggersProperty

dryRun = true

println 'Listing all org jobs with timer set'
if (!dryRun){
  println 'Will also disable Child Scan Trigger timer setting \n'
}

Jenkins.instance.getAllItems(jenkins.branch.OrganizationFolder.class).each {
  if (it.properties.any{ t -> t.getClass() == OrganizationChildTriggersProperty }){
  	println it.fullName + " has a child timer"
    if (!dryRun){
      for (prop in it.properties) {
        if (prop.getClass() == OrganizationChildTriggersProperty){
			println "Removing Child Scan Trigger"
            it.properties.remove(prop)
          	it.save()
        }
      }
    }
  }
}
  
null
