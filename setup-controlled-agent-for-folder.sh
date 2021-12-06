# Inputs:
# JENKINS_URL, AUTH, Folder path, Agent name, jenkins-cli.jar path (optional, assumes jar is in current directory)
if [ $# -lt 4 ]
  then
    echo "Usage: sh setup-controlled-agent.sh JENKINS_URL AUTH FOLDER_PATH AGENT_NAME CLI_JAR_PATH (optional, assumes jar is in current directory)"
    echo "Parameters:"
    echo "    JENKINS_URL  : the CloudBees CI controller URL, i.e. 'https://jenkins.mydomain.com/controller1' "
    echo "    AUTH         : username and api token for user with administrator access to Jenkins, i.e. 'username:apitoken' "
    echo "    FOLDER_PATH  : path to folder on the Jenkins controller, i.e. 'top-level-folder/level-2-folder' "
    echo "    AGENT_NAME   : the name of the static agent where jobs in FOLDER_PATH folder should be allowed to execute, i.e. 'special-team-agent-1' "
    echo "    CLI_JAR_PATH : (optional) full path to the jenkins-cli.jar on the machine where the script is running. Defaults to './jenkins-cli.jar' "
    echo '                   jenkins-cli.jar can be downloaded from $JENKINS_URL/jnlpJars/jenkins-cli.jar'
    exit 1
fi

JENKINS_URL="$1"
AUTH="$2"
FOLDER="$3"
AGENT_NAME="$4"
CLI_JAR=${5:-./jenkins-cli.jar}

echo "Setting up Controlled Agent for folder: $FOLDER on agent: $AGENT_NAME"
TOKEN=$(java -jar "$CLI_JAR" -s "$JENKINS_URL" -auth "$AUTH" create-controlled-slaves-request "$FOLDER")
echo "Token: $TOKEN"
SECRET=$(java -jar "$CLI_JAR" -s "$JENKINS_URL" -auth "$AUTH" approve-controlled-slaves-request "$AGENT_NAME" "$TOKEN")
echo "Secret: $SECRET"
java -jar "$CLI_JAR" -s "$JENKINS_URL" -auth "$AUTH" complete-controlled-slaves-request "$FOLDER" "$TOKEN" "$SECRET"
echo "$FOLDER folder on agent $AGENT_NAME setup complete."
