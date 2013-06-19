#!/bin/bash

JAVA=/usr/local/jdk7/bin/java
MV_HOME=/d3/projects/METViewer/src/apps/verif/metviewer

# construct the classpath for MVBatch
CLASSPATH=$MV_HOME/lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/build

$JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.MVBatch $@
