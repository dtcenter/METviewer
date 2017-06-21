#!/bin/bash

JAVA=/usr/local/jdk/bin/java
MV_HOME=/d3/projects/METViewer/src/apps/METViewer

# construct the classpath for MVBatch
CLASSPATH=$MV_HOME/lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/bonecp-0.7.1.RELEASE.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-api-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/guava-14.0.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.8.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.8.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.8.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer.jar

$JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.MVBatch $@
