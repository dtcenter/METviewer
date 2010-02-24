#!/bin/bash

JAVA=/d1/pgoldenb/usr/local/bin/java
METVIEWER=/home/pgoldenb/apps/verif/metviewer

HOST=taku
WORKS=true
JOB=valid24
JVM_ARGS="-XX:-UseGCOverheadLimit"
#JVM_ARGS="-Xincgc -XX:-UseGCOverheadLimit"

#DATES="20100117b 20100124b 20100131b 20100207b 20100214b"
DATES="20100221b"

if [ $# -gt 0 ] ; then
	JOB=$1
	shift
fi

if [ $# -gt 0 ] ; then
	DATES=$@
fi

# construct the classpath for MVBatch
CLASSPATH=$METVIEWER/lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:$METVIEWER/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$METVIEWER/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$METVIEWER/bin

$JAVA -classpath $CLASSPATH -Xmx2048M $JVM_ARGS edu.ucar.metviewer.MVBatch $HOST $WORKS $JOB $DATES
