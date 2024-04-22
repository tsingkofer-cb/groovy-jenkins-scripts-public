# When running this script, ensure that 2 additional files are present in the same file location:
# - find-non-compliant-remoting-versions.py
# - controllers.txt (containing one controller URL per line, ending with a single blank line.)

USERNAME=
PASSWORD=

while read URL; do
  HOST=$(echo ${URL:8} | tr "/" -)
  echo "Collecting agent remoting version data for ${HOST}..."
  python3 find-non-compliant-remoting-versions.py --user ${USERNAME} --password ${PASSWORD} --useCrumb --controllerUrl ${URL} > ${HOST}-agents.csv
done <controllers.txt

