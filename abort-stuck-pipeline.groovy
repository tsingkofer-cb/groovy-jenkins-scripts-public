Jenkins.instance.getItemByFullName("full/job/name") //path to job, including folder names.
    .getBuildByNumber(100) //build number
    .finish(hudson.model.Result.ABORTED, new java.io.IOException("Aborting build")
); 
