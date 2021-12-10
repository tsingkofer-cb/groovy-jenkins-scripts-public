# Inputs:
# JENKINS_URL, AUTH, CSV input file path, jenkins-cli.jar path (optional, assumes jar is in current directory)
if [ $# -lt 3 ]
  then
    echo "Usage: sh setup-controlled-agent.sh JENKINS_URL AUTH FOLDER_PATH AGENT_NAME CLI_JAR_PATH (optional, assumes jar is in current directory)"
    echo "Parameters:"
    echo "    JENKINS_URL  : the CloudBees CI controller URL, i.e. 'https://jenkins.mydomain.com/controller1' "
    echo "    AUTH         : username and api token for user with administrator access to Jenkins, i.e. 'username:apitoken' "
    echo "    CSV_PATH     : full path to csv input file containing folder/agent mappings "
    echo "    CLI_JAR_PATH : (optional) full path to the jenkins-cli.jar on the machine where the script is running. Defaults to './jenkins-cli.jar' "
    echo '                   jenkins-cli.jar can be downloaded from $JENKINS_URL/jnlpJars/jenkins-cli.jar'
    exit 1
fi

JENKINS_URL="$1"
AUTH="$2"
CSV_PATH="$3"
CLI_JAR=${4:-./jenkins-cli.jar}


while IFS=, read -r AGENT_NAME FOLDER; do 
    echo "Setting up Controlled Agent for folder: $FOLDER on agent: $AGENT_NAME"
    TOKEN=$(java -jar "$CLI_JAR" -s "$JENKINS_URL" -auth "$AUTH" create-controlled-slaves-request "$FOLDER" < /dev/null )
    echo "Token: $TOKEN"
    SECRET=$(java -jar "$CLI_JAR" -s "$JENKINS_URL" -auth "$AUTH" approve-controlled-slaves-request "$AGENT_NAME" "$TOKEN" < /dev/null )
    echo "Secret: $SECRET"
    java -jar "$CLI_JAR" -s "$JENKINS_URL" -auth "$AUTH" complete-controlled-slaves-request "$FOLDER" "$TOKEN" "$SECRET" < /dev/null
    echo "$FOLDER folder on agent $AGENT_NAME setup complete."
    echo ' '
done < "$CSV_PATH"
