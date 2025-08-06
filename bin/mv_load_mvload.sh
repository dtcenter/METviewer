#!/bin/bash

# get the METviewer home directory. It should be one level up from the current dir
# or if the link was used - the original directory

# get the real script path

SOURCE="${BASH_SOURCE[0]}"

while [ -h "$SOURCE" ]; do
  # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

MV_HOME="$(dirname "$DIR")"


#--------------------
# jar files needed (latest versions are in all_jars_for_classpath.sh)
#--------------------
# guava-14.0.1.jar
# log4j-api-2.17.1.jar
# log4j-core-2.17.1.jar
# log4j-iostreams-2.17.1.jar
# mariadb-java-client-2.7.1.jar
# metviewer.jar
# servlet-api.jar
# slf4j-api-1.7.5.jar
# slf4j-log4j12-1.7.5.jar
# tomcat-jdbc-8.5.61.jar
# xercesImpl.jar
# xml-apis.jar

# Construct the classpath for MVLoad from classpath
# categories defined in the all_jars_for_classpath.sh
source all_jars_for_classpath.sh
CLASSPATH=$CLASSPATH_COMMON:$CLASSPATH_MV
echo -e " CLASSPATH for mv_load_mv: \n$CLASSPATH\n"

java -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.MVLoad $@
