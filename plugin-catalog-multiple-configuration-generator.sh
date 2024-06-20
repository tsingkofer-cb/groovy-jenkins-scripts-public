# This basic script can be used alongside (place it in the same directory) of the run.sh script from https://github.com/kyounger/casc-plugin-dependency-calculation
# 
# Usage: bash multiple-version-wrapper.sh $PATH_TO_PLUGINS_YAML $VERSIONS_LIST
# example: bash multiple-version-wrapper.sh plugins.yaml 2.401.2.3,2.414.2.2,2.452.2.3

for VERSION in $(echo $2 | sed "s/,/ /g")
do
    bash run.sh -t mm -v $VERSION -f $1
    FILES+="target/$VERSION/mm/plugin-catalog.yaml "
done
echo "$FILES"
mkdir -p merged
yq eval-all '. as $item ireduce ({}; . *+ $item)' $FILES > merged/plugin-catalog.yaml

# if you want to remove the automatic comments from the final file, use this line
yq -i '... comments=""' merged/plugin-catalog.yaml
