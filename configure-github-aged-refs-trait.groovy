import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.scm_filter.GitHubAgedRefsTrait;

dryRun = true

println 'Setting age limit for Github Organization jobs...'
Jenkins.instance.getAllItems(OrganizationFolder.class).each {
  println it.fullName
  sources = it.getNavigators()
  for (source in sources){
    traits = source.getTraits()
    if (!containsGitHubAgedRefsTrait(traits)) {
      println '  No age limit currently set, adding the default.'
      newTraits = new ArrayList()
      for (t in traits) {newTraits.add(t)}
      newTraits.add(new GitHubAgedRefsTrait('60'))
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
    if (!containsGitHubAgedRefsTrait(traits)) {
      println '  No age limit currently set, adding the default.'
      newTraits = new ArrayList()
      for (t in traits) {newTraits.add(t)}
      newTraits.add(new GitHubAgedRefsTrait('60'))
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
  
def boolean containsGitHubAgedRefsTrait(List list) {
    for ( item in list) {
        if (item instanceof org.jenkinsci.plugins.scm_filter.GitHubAgedRefsTrait) {
            return true;
        }
    }
    return false;
}
