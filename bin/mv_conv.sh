#!/bin/bash

JAVA=/usr/local/jdk/bin/java
MV_HOME=/d3/projects/METViewer/src/apps/verif/metviewer

# construct the classpath for MVLoad
CLASSPATH=$MV_HOME/lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/bin

$JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.MVConv $@
