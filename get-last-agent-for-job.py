import requests, getpass, argparse
from requests.auth import HTTPBasicAuth

parser = argparse.ArgumentParser(description="Retrieve all credentials by folder location")
parser.add_argument('--user', help='CI username', required=True)
parser.add_argument('--token', help='CI API token, will prompt if not provided')
parser.add_argument('--url', help='CI Controller URL', required=True)

args = parser.parse_args()

if not args.token:
    p = getpass.getpass()
else:
    p = args.token

auth = HTTPBasicAuth(args.user, p)

# groovy script to collect all Job type items on a controller
jobsGroovy = "Jenkins.instance.getAllItems(Job.class).each{println Jenkins.instance.getRootUrl() + it.url};return null;"
scriptData = { "script" : jobsGroovy }

# Execute groovy script to get all jobs within the CI instance
jobsResponse = requests.post(args.url + '/scriptText', auth=auth, data=scriptData).text
allJobs = [y for y in (x.strip() for x in jobsResponse.splitlines()) if y]

# For each job on the controller, retrieve the logs and capture the line that shows the agent name.
for job in allJobs:
    lastBuildConsole = requests.get(job + 'lastSuccessfulBuild/consoleText', auth=auth)
    if lastBuildConsole.status_code == 200:
        lastBuildConsoleText = lastBuildConsole.text
        agentNameStartLocation = lastBuildConsoleText.find('Running on ')
        if agentNameStartLocation != -1:
            agentNameEndLocation = lastBuildConsoleText.find(' in', agentNameStartLocation)
            print(job + ',' + lastBuildConsoleText[agentNameStartLocation+11:agentNameEndLocation])
        else:
            print(job + ',' + "Agent name not found in console text.")
    else:
        print(job + ',' + "No Last Successful build to parse.")