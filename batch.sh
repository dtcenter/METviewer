#!/bin/bash

JAVA=/d1/pgoldenb/usr/local/bin/java
METVIEWER=/home/pgoldenb/apps/verif/metviewer

HOST=$(hostname)
WORKS=false
JOB=valid24
JVM_ARGS="-XX:-UseGCOverheadLimit"
#JVM_ARGS="-Xincgc -XX:-UseGCOverheadLimit"

#DATES="2010022118V_06h 2010022200V_06h 2010022206V_06h 2010022212V_06h 2010022212V_24h"
#DATES="20100117b 20100124b 20100131b 20100207b 20100214b 20100221b"
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
