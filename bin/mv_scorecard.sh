#!/bin/bash

if [[ ! -z "${JAVA_HOME}" ]]; then
    JAVA_HOME=/usr/local/jdk
fi
JAVA=${JAVA_HOME}/bin/java

if [[ ! -z "${MV_HOME}" ]]; then
    MV_HOME=/d3/projects/METViewer/src/apps/METviewer
fi
# construct the classpath for MVBatch
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/tomcat-jdbc-8.5.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/juli-6.0.53.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-api-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/guava-14.0.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/j2html-0.7.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/jackson-core-2.8.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/jackson-databind-2.8.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/commons-lang3-3.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/servlet-api.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer.jar


$JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.scorecard.Scorecard $@
