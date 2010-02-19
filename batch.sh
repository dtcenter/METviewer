#!/bin/bash

HOST=kemosab
WORKS=false
JOB=30day24

#DATES="20100117b 20100124b 20100131b 20100207b 20100214b"
DATES=""

if [ $# -gt 0 ] ; then
	JOB=$1
	shift
fi

if [ $# -gt 0 ] ; then
	DATES=$@
fi

# construct the classpath for MVBatch
CLASSPATH=lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:bin

java -classpath $CLASSPATH -Xmx1024M edu.ucar.metviewer.MVBatch $HOST $WORKS $JOB $DATES
