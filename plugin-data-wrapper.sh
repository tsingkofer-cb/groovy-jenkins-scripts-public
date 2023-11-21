# When running this script, ensure that 2 additional files are present in the same file location:
# - get-plugin-data.py  
# - controllers.txt (containing one controller URL per line, ending with a single blank line.)

USERNAME=
PASSWORD=
CI_VERSION=2.414.3.8 #Set CBCI version to check against for CAP status/tier

while read URL; do
  HOST=$(echo ${URL} | awk -F[/:] '{print $4}')
  echo "Collecting plugin data for ${HOST}..."
  python3 get-plugin-data.py --user ${USERNAME} --password ${PASSWORD} --useCrumb --controllerUrl ${URL} --ciVersion ${CI_VERSION} > ${HOST}-plugins.csv
done <controllers.txt
