#!/bin/bash

# construct the classpath for MVLoad
CLASSPATH=lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:bin

java -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.MVLoad $@
