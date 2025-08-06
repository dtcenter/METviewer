#!/bin/bash

# Define all the CLASSPATHs with the jar files and their versions in one location
# rather than spreading this out (and repeating) amongst the mv_batch.sh,
# mv_scorecard, mv_prune.sh, ..., mv_test.sh scripts.

# Get the METviewer home directory.  Expected to be one level up from the
# current directory.  If link was used, then assume the original directory.

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

# jars common to all scripts
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/guava-33.4.8.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/log4j-api-2.17.1.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/log4j-core-2.17.1.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/log4j-iostreams-2.17.1.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/mariadb-java-client-2.7.1.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/servlet-api.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/slf4j-api-1.7.5.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/tomcat-jdbc-8.5.61.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/xercesImpl.jar
CLASSPATH_COMMON=$CLASSPATH_COMMON:$MV_HOME/lib/xml-apis.jar

# jars used for testing (mv_test.sh and mv_compare.sh)
CLASSPATH_TESTING=$CLASSPATH_TESTING:${MV_HOME}/lib/commons-fileupload-1.4.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:${MV_HOME}/lib/commons-io-2.8.0.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:$MV_HOME/lib/commons-lang3-3.11.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:${MV_HOME}/lib/hamcrest-core-1.3.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:${MV_HOME}/lib/junit-4.11.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:$MV_HOME/lib/j2html-1.4.0.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:$MV_HOME/lib/jackson-core-2.12.0.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:$MV_HOME/lib/jackson-databind-2.12.0.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:${MV_HOME}/dist/lib/metviewer_all.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:${MV_HOME}/lib/mockito-all-1.9.5.jar
CLASSPATH_TESTING=$CLASSPATH_TESTING:${MV_HOME}/lib/servlet-api-4.0.1.jar


# metviewer
CLASSPATH_MV=$CLASSPATH_MV:$MV_HOME/dist/lib/metviewer.jar

#snakeyaml
CLASSPATH_SNAKE=$CLASSPATH_SNAKE:$MV_HOME/lib/snakeyaml-1.27.jar





