import json, requests, sys, getpass
from requests.auth import HTTPBasicAuth
import argparse

parser = argparse.ArgumentParser(description="Collect plugin data from Jenkins OSS.")
parser.add_argument('--user', help='CI username', required=True)
parser.add_argument('--password', help='CI API token, will prompt if not provided')
parser.add_argument('--useCrumb', action='store_true', help='Enable CSRF crumb support if not using an API token.')
parser.add_argument('--controllerUrl', help='Jenkins Controller URLs', required=True)

args = parser.parse_args()

if not args.password:
    p = getpass.getpass()
else:
    p = args.password

auth = HTTPBasicAuth(args.user, p) 

controllerUrl = args.controllerUrl

#for each controller, get embedded remoting version
remotingVersionGroovy = 'import jenkins.slaves.RemotingVersionInfo;println RemotingVersionInfo.getEmbeddedVersion();'
scriptData = { "script" : remotingVersionGroovy }

if args.useCrumb:
    session = requests.session()
    req = session.get(controllerUrl + '/crumbIssuer/api/json', auth=auth).json()
    crumb = {req['crumbRequestField']: req['crumb']}
    headers = {'Content-Type': 'application/x-www-form-urlencoded'}
    headers.update(crumb)
    remotingResponse = session.post(controllerUrl + '/scriptText', auth=auth, data=scriptData, headers=headers)
    agentData = session.get(controllerUrl + '/computer/api/json', auth=auth)
else:
    remotingResponse = requests.post(controllerUrl + '/scriptText', auth=auth, data=scriptData)
    if remotingResponse.status_code == 403:
        raise Exception("Request returned 403, set --useCrumb flag if authenticating with username/password instead of API token.")
    agentData = requests.get(controllerUrl + '/computer/api/json', auth=auth)

embeddedRemotingVersion = remotingResponse.text.strip()
# print(embeddedRemotingVersion)
agentsJson = json.loads(agentData.text)
agents = agentsJson["computer"]
print('Agent Name,Remoting Version,Compliant')
for agent in agents:
    agentName = agent["displayName"]
    agentRemotingVersion = agent["monitorData"]["hudson.plugin.versioncolumn.VersionMonitor"]
    if agentRemotingVersion is None:
        agentRemotingVersion = 'Agent offline'
    compliant = 'YES'
    if embeddedRemotingVersion != agentRemotingVersion:
       compliant = 'NO'
    print(agentName + ',' + agentRemotingVersion + ',' + compliant)