//Will reindex all jobs that have the BitbucketAgedRefsTrait set.

import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import jenkins.branch.OrganizationFolder;
import org.jenkinsci.plugins.scm_filter.BitbucketAgedRefsTrait;

dryRun = true

println 'Reindexing Bitbucket Organization jobs...'
Jenkins.instance.getAllItems(OrganizationFolder.class).each {
  println it.fullName
  sources = it.getNavigators()
  for (source in sources){
    traits = source.getTraits()
    if (containsBitbucketAgedRefsTrait(traits)) {
      println '  Aged Ref has already been set, reindexing now.'
      if(!dryRun){
        it.getComputation().run()
        println '    ...done.'
      }
    } else {
      println '  No Aged Ref setting present, skipping reindex.'
    }
  }
}

println '\nReindexing age limit for multi-branch pipeline jobs...'
Jenkins.instance.getAllItems(WorkflowMultiBranchProject.class).each {
  println it.fullName
  sources = it.getSources()
  for (source in sources){
    scmSource = source.getSource()
    traits = scmSource.getTraits()
    if (containsBitbucketAgedRefsTrait(traits)) {
      println '  Aged Ref has been set, reindexing now.'
      if(!dryRun){
        it.getIndexing().run()
        println '    ...done.'
      }
    } else {
      println '  No Aged Ref setting present, skipping reindex.'
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
