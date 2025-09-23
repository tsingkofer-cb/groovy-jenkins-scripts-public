//Will add the BitbucketAgedRefsTrait to multibranch pipelines that don't already have one set. Assumes you are only using Bitbucket Branch Sources on the controller, not any GitHub.

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.scm_filter.BitbucketAgedRefsTrait;

dryRun = true

println 'Setting age limit for Bitbucket Organization jobs...'
Jenkins.instance.getAllItems(OrganizationFolder.class).each {
  println it.fullName
  sources = it.getNavigators()
  for (source in sources){
    traits = source.getTraits()
    if (!containsBitbucketAgedRefsTrait(traits)) {
      println '  No age limit currently set, adding the default.'
      newTraits = new ArrayList()
      for (t in traits) {newTraits.add(t)}
      newTraits.add(new BitbucketAgedRefsTrait('60'))
      if(!dryRun){
        source.setTraits(newTraits)
        it.save()
        it.getComputation().run()
      }
    } else {
      println '  Branch age limit is already set.'
    }
  }
}

println '\nSetting age limit for multi-branch pipeline jobs...'
Jenkins.instance.getAllItems(WorkflowMultiBranchProject.class).each {
  println it.fullName
  sources = it.getSources()
  for (source in sources){
    scmSource = source.getSource()
    traits = scmSource.getTraits()
    if (!containsBitbucketAgedRefsTrait(traits)) {
      println '  No age limit currently set, adding the default.'
      newTraits = new ArrayList()
      for (t in traits) {newTraits.add(t)}
      newTraits.add(new BitbucketAgedRefsTrait('60'))
      if(!dryRun){
        scmSource.setTraits(newTraits)
        it.save()
        it.getIndexing().run()
      }
    } else {
      println '  Branch age limit is already set.'
    }
  }
}
return
  
def boolean containsBitbucketAgedRefsTrait(List list) {
    for ( item in list) {
        if (item instanceof org.jenkinsci.plugins.scm_filter.BitbucketAgedRefsTrait) {
            return true;
        }
    }
    return false;
}
