import json, requests, sys, getpass
from requests.auth import HTTPBasicAuth
import argparse

parser = argparse.ArgumentParser(description="Collect plugin data from Jenkins OSS.")
parser.add_argument('--user', help='CI username', required=True)
parser.add_argument('--password', help='CI API token, will prompt if not provided')
parser.add_argument('--useCrumb', action='store_true', help='Enable CSRF crumb support if not using an API token.')
parser.add_argument('--controllerUrl', help='File containing Jenkins Controller URLs', required=True)
parser.add_argument('--ciVersion', help='CBCI version to check against for CAP status/tier', required=True)

args = parser.parse_args()

if not args.password:
    p = getpass.getpass()
else:
    p = args.password

auth = HTTPBasicAuth(args.user, p) 

controllerUrl = args.controllerUrl

#Set CBCI version to check against for CAP status/tier
ciVersion = args.ciVersion #'2.414.3.8'

#collect update center json for tier data
cbUcResponse = requests.get('https://jenkins-updates.cloudbees.com/update-center/envelope-core-mm/update-center.json?version=' + ciVersion).text
cbUcResponse = cbUcResponse[19:]
cbUcJson = json.loads(cbUcResponse[:-4])

#collect plugin health score json
pluginHealthJson = requests.get('https://plugin-health.jenkins.io/api/scores').json()

#for each controller, get plugins installed
pluginsGroovy = 'Jenkins.instance.pluginManager.plugins.each{plugin -> println ("${plugin.getShortName()}:${plugin.getVersion()}")};return null;'
scriptData = { "script" : pluginsGroovy }

if args.useCrumb:
    session = requests.session()
    req = session.get(controllerUrl + '/crumbIssuer/api/json', auth=auth).json()
    crumb = {req['crumbRequestField']: req['crumb']}
    headers = {'Content-Type': 'application/x-www-form-urlencoded'}
    headers.update(crumb)
    pluginResponse = session.post(controllerUrl + '/scriptText', auth=auth, data=scriptData, headers=headers)
else:
    pluginResponse = requests.post(controllerUrl + '/scriptText', auth=auth, data=scriptData)
    if pluginResponse.status_code == 403:
        raise Exception("Request returned 403, set --useCrumb flag if authenticating with username/password instead of API token.")

allPlugins = [y for y in (x.strip() for x in pluginResponse.text.splitlines()) if y]

#print csv column headers
print('Name,Version,Last Release Date,Total Installs,Health Score,Plugin Tier')

#for each plugin, get data from plugins.jenkins.io
for plugin in allPlugins:
    split = plugin.split(":")
    name = split[0]
    version = split[1]
    pluginDataResponse = requests.get('https://plugins.jenkins.io/api/plugin/' + name).json()
    buildDate = pluginDataResponse['buildDate']
    currentInstalls = pluginDataResponse['stats']['currentInstalls']
    healthScore = pluginHealthJson['plugins'][name]['value']
    tier = 'community'
    if name in cbUcJson['envelope']['plugins']:
        tier = cbUcJson['envelope']['plugins'][name]['tier']
    print(name+','+version+','+buildDate+','+str(currentInstalls)+','+str(healthScore)+','+tier)
