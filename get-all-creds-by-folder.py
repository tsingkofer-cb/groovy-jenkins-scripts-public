import json, requests, sys, getpass
from requests.auth import HTTPBasicAuth
import argparse

parser = argparse.ArgumentParser(description="Retrieve all credentials by folder location")
parser.add_argument('--user', help='CI username', required=True)
parser.add_argument('--password', help='CI API token, will prompt if not provided')
parser.add_argument('--url', help='CI Controller URL', required=True)

args = parser.parse_args()

if not args.password:
    p = getpass.getpass()
else:
    p = args.password

auth = HTTPBasicAuth(args.user, p)

def findFolders(items):
    folderList = []
    for item in items["jobs"]:
        if item["_class"] == "com.cloudbees.hudson.plugins.folder.Folder":
            folderList.append(item["url"])
    return folderList

# Get all folders within the CI instance
topLevelItems = requests.get(args.url + '/api/json?tree=jobs[url]', auth=auth).json()

checkedFolders = []
# Iterate through items to find folders
allFolders = findFolders(topLevelItems)
for folder in allFolders:
    #print(folder)
    subfolders = requests.get(folder + '/api/json?tree=jobs[url]', auth=auth).json()
    childResults = findFolders(subfolders)
    if childResults != []:
        for i in childResults:
            allFolders.append(i)
    checkedFolders.append(folder)

for folder in checkedFolders:
    print("Folder: " + folder)
    folderResponse = requests.get(folder + 'credentials/api/json?tree=stores[domains[credentials[id,description,typeName]]]', auth=auth).json()
    folderCredentials = folderResponse["stores"]["folder"]["domains"]["_"]["credentials"]
    if not folderCredentials:
        print("    No Credentials have been created on this folder.")
        print('')
    for cred in folderCredentials:
        print('    ID: ' + cred["id"])
        print('    Description: ' + cred["description"])
        print('    Credential Type: ' + cred["typeName"])
        print('')