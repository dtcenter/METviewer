#!/bin/bash

LIST_SUNDAYS="20100117b 20100124b 20100131b 20100207b 20100214b"

# construct the classpath for MVBatch
CLASSPATH=lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:bin

java -classpath $CLASSPATH -Xmx1024M MVBatch kemosabe false 30day24 $LIST_SUNDAYS
#java -classpath $CLASSPATH MVBatch $@
