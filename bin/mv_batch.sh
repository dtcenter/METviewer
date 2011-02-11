#!/bin/bash

JAVA=/d1/pgoldenb/usr/local/bin/java
METVIEWER=/home/pgoldenb/apps/verif/metviewer

# construct the classpath for MVBatch
CLASSPATH=$METVIEWER/lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:$METVIEWER/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$METVIEWER/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$METVIEWER/bin

$JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.MVBatch $@
