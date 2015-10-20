#!/bin/bash

JAVA=/usr/local/jdk/bin/java
MV_HOME=/d3/projects/METViewer/src_dev/apps/verif/metviewer

# construct the classpath for MVLoad
CLASSPATH=$MV_HOME/lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/bonecp-0.7.1.RELEASE.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-api-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/guava-14.0.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/junit-4.11.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/hamcrest-core-1.3.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/commons-io-2.4.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mockito-all-1.9.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/servlet-api.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/commons-fileupload-1.3.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer_all.jar

ARGS=1

if [ 1 -ne $# ]
then
  echo "Running plots scripts"
  $JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.test.AllTestRunner

else
  if [[ $1 == "all" ]];
  then
    echo "Running all scripts"
    $JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.test.AllTestRunner all
  else
    if [[ $1 == "plots" ]];
    then
      echo "Running plots scripts"
      $JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.test.AllTestRunner
    fi
  fi
fi



