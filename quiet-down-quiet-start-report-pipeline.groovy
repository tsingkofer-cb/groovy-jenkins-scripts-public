pipeline {
    agent {...}
    environment {
        HOSTNAME = "https://my-ci-hostname.com"
    }
    stages {
        stage('Main') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'oc-api-token', passwordVariable: 'APITOKEN', usernameVariable: 'USERNAME')]) {
                    //get list of controllers that are currently online
                    sh '''set +x; curl -s -d "script=def mms = Jenkins.instance.getAllItems(com.cloudbees.opscenter.server.model.ManagedMaster);mms.each {if (it.isOnline()){println it.getConfiguration().domain}}; null;" --user $USERNAME:$APITOKEN ${HOSTNAME}/cjoc/scriptText > controllers.txt
                    cat controllers.txt
                    '''

                    //check for quiet down and quiet start state for each controller, generate and archive a csv, and print as a table in the build logs.
                     sh '''set +x;
                     echo 'CONTROLLER,QUIET_DOWN,QUIET_START' > report.csv
                     while read -r controller; do
                       QUIETDOWN=$(curl -s -d 'script=import hudson.*;println Jenkins.getInstance().isQuietingDown().toString();null' --user $USERNAME:$APITOKEN ${HOSTNAME}/${controller}/scriptText)
                       QUIETSTART=$(curl -s -d 'script=import hudson.*;println ExtensionList.lookupSingleton(com.cloudbees.jenkins.plugins.quietStart.QuietStartConfiguration).isActivated().toString();null' --user $USERNAME:$APITOKEN ${HOSTNAME}/${controller}/scriptText)
                       echo -e "${controller},${QUIETDOWN},${QUIETSTART}" >> report.csv
                     done < controllers.txt
                     echo ''
                     column -t -s ',' report.csv
                     echo ''
                     '''
                     archiveArtifacts 'report.csv'
                }
            }
        }
    }
}
