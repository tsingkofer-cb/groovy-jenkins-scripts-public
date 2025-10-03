import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.scm_filter.BitbucketAgedRefsTrait;
import org.jenkinsci.plugins.scm_filter.GitHubAgedRefsTrait;

dryRun = false
ageLimitDays = '60'

println 'Setting age limit for Organization jobs...'
Jenkins.instance.getAllItems(OrganizationFolder.class).each {
  println it.fullName
  sources = it.getNavigators()
  for (source in sources){
    println '  ' + source.getClass()
    if(source.getClass() == org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator){
        traits = source.getTraits()
        if (!containsGitHubAgedRefsTrait(traits)) {
          println '  No age limit currently set, adding the GitHub default.'
          newTraits = new ArrayList()
          for (t in traits) {newTraits.add(t)}
          newTraits.add(new GitHubAgedRefsTrait(ageLimitDays))
          if(!dryRun){
            source.setTraits(newTraits)
            it.save()
            it.getComputation().run()
          }
        } else {
          println '  Branch age limit is already set.'
        }
    } else if (source.getClass() == com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMNavigator){
        traits = source.getTraits()
        if (!containsBitbucketAgedRefsTrait(traits)) {
          println '  No age limit currently set, adding the Bitbucket default.'
          newTraits = new ArrayList()
          for (t in traits) {newTraits.add(t)}
          newTraits.add(new BitbucketAgedRefsTrait(ageLimitDays))
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
}

println '\nSetting age limit for multi-branch pipeline jobs...'
Jenkins.instance.getAllItems(WorkflowMultiBranchProject.class).each {
  println it.fullName
  sources = it.getSources()
  for (source in sources){
    scmSource = source.getSource()
    println '  ' + scmSource.getClass()
    if(scmSource.getClass() == org.jenkinsci.plugins.github_branch_source.GitHubSCMSource){
      traits = scmSource.getTraits()
      if (!containsGitHubAgedRefsTrait(traits)) {
        println '  No age limit currently set, adding the GitHub default.'
        newTraits = new ArrayList()
        for (t in traits) {newTraits.add(t)}
        newTraits.add(new GitHubAgedRefsTrait(ageLimitDays))
        if(!dryRun){
          scmSource.setTraits(newTraits)
          it.save()
          it.getIndexing().run()
        }
      } else {
        println '  Branch age limit is already set.'
      }
    } else if (scmSource.getClass() == com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource){
      traits = scmSource.getTraits()
      if (!containsBitbucketAgedRefsTrait(traits)) {
        println '  No age limit currently set, adding the Bitbucket default.'
        newTraits = new ArrayList()
        for (t in traits) {newTraits.add(t)}
        newTraits.add(new BitbucketAgedRefsTrait(ageLimitDays))
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
               
def boolean containsGitHubAgedRefsTrait(List list) {
    for ( item in list) {
        if (item instanceof org.jenkinsci.plugins.scm_filter.GitHubAgedRefsTrait) {
            return true;
        }
    }
    return false;
}
