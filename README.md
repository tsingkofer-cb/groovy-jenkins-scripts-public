# groovy-jenkins-scripts-public
 Collection of scripts used to make usage of CloudBees CI more efficient.


## Jenkins OSS Plugin Data Collection Scripts
### get-plugin-data.py
This script can be run against a single Jenkins controller and will output data points for each plugin that is installed in a CSV format, so it can easily be output to a file and then opened in Excel for easy sorting and filtering.

Data points captured for each plugin:
- Name
- Version
- Last Release Date (From Jenkins plugin site)
- Total Installs (From Jenkins plugin site)
- Plugin Health Score (From Jenkins plugin site)
- Plugin tier (From CloudBees Update Center)


Syntax to run the script (if `--password` is omitted, the script will prompt for it at runtime, so that the secret won't be retained in the terminal history.):

`python3 get-plugin-data.py --user ${USERNAME} --password ${API_TOKEN} --controllerUrl http://${HOSTNAME} --ciVersion 2.414.3.8 > ${HOSTNAME}.csv`

If you need to use Username/Password for authentication, you can just add the `--useCrumb` argument to the command to run it. 

`python3 get-plugin-data.py --user ${USERNAME} --password ${PASSWORD} --useCrumb --controllerUrl http://${HOSTNAME} --ciVersion 2.414.3.8 > ${HOSTNAME}.csv`

### plugin-data-wrapper.sh
This is a wrapper script for `get-plugin-data.py` that you can use to collect the data from multiple Jenkins controllers in a single run. In the script, you need to provide a username, password, and the version of CI to use when generating the CAP status for each plugin. Then, collect all the controller urls in a txt file named controllers.txt (containing one URL per line, and ending with a single blank line) and place it and `get-plugin-data.py` in the same directory, alongside this wrapper script. Running it will output a separate csv file into the current directory for each controller in the list, named `{HOSTNAME}-plugins.csv`.

 Additionally, the script will create a consolidated and de-duplicated `plugin-report.csv` containing the full list of plugins installed across all controllers, as well as a `Number of Controllers` field which shows how many controllers a given plugin version is installed on.