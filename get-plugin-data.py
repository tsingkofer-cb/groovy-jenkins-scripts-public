import json, requests, sys, getpass, csv, concurrent.futures
from requests.auth import HTTPBasicAuth
import click
from tqdm import tqdm
import time

PLUGIN_API_URL = 'https://plugins.jenkins.io/api/plugin/'
PLUGIN_HEALTH_URL = 'https://plugin-health.jenkins.io/api/scores'
UPDATE_CENTER_URL = 'https://jenkins-updates.cloudbees.com/update-center/envelope-core-mm/update-center.json?version='
PLUGIN_GROOVY = 'Jenkins.instance.pluginManager.plugins.each{plugin -> println ("${plugin.getShortName()}:${plugin.getVersion()}")};return null;'
FIELDNAMES = ['Name', 'Version', 'Last Release Date', 'Total Installs', 'Health Score', 'Plugin Tier', 'Active CVEs']

def get_cb_uc_json(ciVersion):
    cbUcResponse = requests.get(f"{UPDATE_CENTER_URL}{ciVersion}").text
    cbUcResponse = cbUcResponse[19:]
    cbUcJson = json.loads(cbUcResponse[:-4])
    return cbUcJson

def get_plugin_health_json():
    return requests.get(PLUGIN_HEALTH_URL).json()
def get_all_plugins(controllerUrl, auth, crumb, scriptData):
    if crumb:
        session = requests.session()
        req = session.get(f"{controllerUrl}/crumbIssuer/api/json", auth=auth)
        if req.status_code != 200:
            print(f"Error fetching data from {controllerUrl}/crumbIssuer/api/json. Please check the URL or your network connection.")
            sys.exit(1)
        req = req.json()
        crumb = {req['crumbRequestField']: req['crumb']}
        headers = {'Content-Type': 'application/x-www-form-urlencoded'}
        headers.update(crumb)
        pluginResponse = session.post(f"{controllerUrl}/scriptText", auth=auth, data=scriptData, headers=headers)
    else:
        pluginResponse = requests.post(f"{controllerUrl}/scriptText", auth=auth, data=scriptData)
        if pluginResponse.status_code == 403:
            raise Exception("Request returned 403, set --useCrumb flag if authenticating with username/password instead of API token.")
        elif pluginResponse.status_code != 200:
            print(f"Error posting data to {controllerUrl}/scriptText. Please check the URL, credentials or network connection.")
            sys.exit(1)
    allPlugins = [y for y in (x.strip() for x in pluginResponse.text.splitlines()) if y]
    return allPlugins
def get_plugin_data(plugin, pluginHealthJson, cbUcJson):
    split = plugin.split(":")
    name = split[0]
    version = split[1]
    try:
        pluginDataResponse = requests.get(f"{PLUGIN_API_URL}{name}").json()
        buildDate = pluginDataResponse['buildDate']
        currentInstalls = pluginDataResponse['stats']['currentInstalls']
        activeCves = 0
        allWarnings = pluginDataResponse['securityWarnings']
        if allWarnings != None:
            for warning in allWarnings:
                if warning['active'] == True:
                    activeCves = activeCves+1
        healthScore = pluginHealthJson['plugins'][name]['value']
        tier = 'community'
        if name in cbUcJson['envelope']['plugins']:
            tier = cbUcJson['envelope']['plugins'][name]['tier']
        return {'Name': name, 'Version': version, 'Last Release Date': buildDate, 'Total Installs': currentInstalls, 'Health Score': healthScore, 'Plugin Tier': tier, 'Active CVEs': activeCves}
    except:
        return {'Name': name, 'Version': version, 'Last Release Date': 'Not Found', 'Total Installs': 'Not Found', 'Health Score': 'Not Found', 'Plugin Tier': 'Not Found', 'Active CVEs': 'Not Found'}

def write_csv(output, plugin_data):
    writer = csv.DictWriter(sys.stdout if output is None else open(output, 'w', newline=''), fieldnames=FIELDNAMES)
    writer.writeheader()
    for data in plugin_data:
        writer.writerow(data)

@click.command()
@click.option('--user', prompt=True, envvar='USER', help='CI username')
@click.option('--password', prompt=True, hide_input=True, envvar='PASSWORD', help='CI API token, will prompt if not provided')
@click.option('--crumb', is_flag=True, help='Enable CSRF crumb support if not using an API token.')
@click.option('--controllerurl', prompt=True, help='Jenkins Controller URLs')
@click.option('--civersion', prompt=True, help='CBCI version to check against for CAP status/tier')
@click.option('--output', default=None, help='Output CSV file')  # Set default to None
def main(user, password, crumb, controllerurl, civersion, output):
    auth = HTTPBasicAuth(user, password)

    with tqdm(total=3, desc="Setting up", bar_format="{l_bar}{bar}| {n_fmt}/{total_fmt}") as pbar:
        try:
            cbUcJson = get_cb_uc_json(civersion)
            pbar.update()
            pluginHealthJson = get_plugin_health_json()
            pbar.update()
            scriptData = { "script" : PLUGIN_GROOVY }
            allPlugins = get_all_plugins(controllerurl, auth, crumb, scriptData)
            pbar.update()

            with concurrent.futures.ThreadPoolExecutor() as executor:
                futures = {executor.submit(get_plugin_data, plugin, pluginHealthJson, cbUcJson): plugin for plugin in allPlugins}
                plugin_data = []
                with tqdm(total=len(allPlugins), desc="Processing plugins", bar_format="{l_bar}{bar}| {n_fmt}/{total_fmt}") as pbar:
                    for future in concurrent.futures.as_completed(futures):
                        plugin = futures[future]
                        try:
                            data = future.result()
                        except Exception as exc:
                            print('%r generated an exception: %s' % (plugin, exc))
                        else:
                            plugin_data.append(data)
                        pbar.update()
                        pbar.set_description(f"Processing plugin: {plugin.ljust(48)}")

            write_csv(output, plugin_data)
        except requests.exceptions.HTTPError as http_err:
            print(f"HTTP error occurred: {http_err}")
            sys.exit(1)
        except requests.exceptions.ConnectionError as conn_err:
            print(f"Error connecting: {conn_err}")
            sys.exit(1)
        except requests.exceptions.Timeout as timeout_err:
            print(f"Timeout error: {timeout_err}")
            sys.exit(1)
        except requests.exceptions.RequestException as err:
            print(f"An error occurred: {err}")
            sys.exit(1)

if __name__ == '__main__':
    main()