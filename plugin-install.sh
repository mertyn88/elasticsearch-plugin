#!/bin/sh

# get define plugin-name
function get_plugin_name() {
  while read -r line
  do
    if [[ $line =~ ^name.*$ ]] ; then
      # foo  <-- from variable foo
      # ##   <-- greedy front trim
      # *    <-- matches anything
      # :    <-- until the last ':'
      echo ${line##*=}
    fi
  done  <  `pwd`"/nori-plugin/src/main/resources/plugin-descriptor.properties"
}

# Set variable
# elasticsearch path
ES_PATH="/Users/junmyung/elasticsearch-7.9.2/bin"

# elasticsearch aleady plugin remove
ES_PLUGIN_NAME=$(get_plugin_name)

# elasticsearch plugin path
ES_PLUGIN_PATH=`pwd`"/nori-plugin/target/nori-plugin-7.9.2.zip"

echo "Maven install Start.."
mvn clean install

echo "Remove plugin "$ES_PLUGIN_NAME
$ES_PATH/elasticsearch-plugin remove $ES_PLUGIN_NAME

echo "Install plugin "$ES_PLUGIN_NAME
$ES_PATH/elasticsearch-plugin install file://$ES_PLUGIN_PATH