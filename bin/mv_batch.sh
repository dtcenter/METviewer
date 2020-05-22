#!/bin/bash


JAVA=${JAVA_HOME}/bin/java

if [ -z ${MV_HOME+x} ]; then
    MV_HOME=/Users/tatiana/git/METviewer
fi
# construct the classpath for MVBatch
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mariadb-java-client-2.4.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/tomcat-jdbc-8.5.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/juli-6.0.53.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-api-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/guava-14.0.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/servlet-api.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/snakeyaml-1.25.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer.jar

$JAVA -classpath $CLASSPATH -Xmx2048M -Dpython.env=/d3/projects/METViewer/METviewer_py3.6.3/ -Dmetcalcpy.home=/d3/projects/METViewer/METcalcpy -Dmetplotpy.home=/d3/projects/METViewer/METplotpy edu.ucar.metviewer.MVBatch $@

