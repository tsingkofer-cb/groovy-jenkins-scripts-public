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

echo 'Consolidating data from all controllers into plugin-report.csv...'
#combine files and remove duplicates (different versions of the same plugin are treated uniquely)
sort -u ./*-plugins.csv > plugins_consolidated.csv
#remove header
tail -n +2 plugins_consolidated.csv > plugins_consolidated.csv.tmp && mv plugins_consolidated.csv.tmp plugins_consolidated.csv 
#add header with additional column for number of controllers with plugin version installed
echo 'Name,Version,Last Release Date,Total Installs,Health Score,Plugin Tier,Number of Controllers' > plugin-report.csv
#collect a count of the number of controllers with each plugin installed and append to row
while read PLUGIN_DATA; do
  COUNT=$(grep -o "$PLUGIN_DATA" ./*-plugins.csv | wc -l | xargs)
  echo "$PLUGIN_DATA,$COUNT" >> plugin-report.csv
done <plugins_consolidated.csv

#cleanup intermediate file
rm -f plugins_consolidated.csv