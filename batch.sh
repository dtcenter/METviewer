#!/bin/bash

# construct the classpath for MVBatch
CLASSPATH=lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:bin

java -classpath $CLASSPATH -Xmx1024M MVBatch $@
#java -classpath $CLASSPATH MVBatch $@
