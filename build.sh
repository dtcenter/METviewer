#!/bin/bash

CLASSPATH=lib/log4j-1.2.15.jar:lib/mysql-connector-java-5.1.6.jar:lib/xercesImpl.jar

javac -classpath $CLASSPATH -source 1.4 java/*.java
mv java/*.class bin
jar cf batch_cron.jar bin/*.class
