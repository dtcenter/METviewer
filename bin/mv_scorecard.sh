#!/bin/bash

# get the METviewer home directory. It should be one level up from the current dir
# or if the link was used - the original directory

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

# construct the classpath for MVBatch
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mariadb-java-client-3.5.6.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/tomcat-jdbc-10.1.46.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/juli-6.0.53.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-api-2.0.17.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-log4j12-2.0.17.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/guava-33.4.8.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/j2html-1.4.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/jackson-core-2.20.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/jackson-databind-2.20.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/commons-lang3-3.19.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.25.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.25.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.25.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/jakarta.servlet-api-5.0.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/snakeyaml-2.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer.jar

PYTHON_ENV=<path_to_python_env>
METCALCPY_HOME=<path_to_metcalcpy_home>

java -classpath $CLASSPATH -Xmx4096M -Dpython.env=$PYTHON_ENV -Dmetcalcpy.home=$METCALCPY_HOME  edu.ucar.metviewer.scorecard.Scorecard $@
