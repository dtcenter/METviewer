#!/bin/bash

#JAVA=/usr/local/jdk/bin/java
JAVA=$(which java)
$($JAVA)
if [ "$?" -ne "0" ]; then
   echo "You do not have a java executible in your path";
   exit;
fi

JAVAC=$(which javac)
$(javac -version)
if [ "$?" -ne "0" ]; then
   echo "You do not have a jdk (java development kit) installed";
   exit;
fi
if [ -z ${MV_HOME+x} ]; then 
	echo "MV_HOME is unset"
	echo "setting it to /d3/projects/METViewer/src_dev/apps/METViewer"
	MV_HOME=/d3/projects/METViewer/src_dev/apps/METViewer
else
	echo "MV_HOME is set to '$MV_HOME'"
fi

# construct the classpath for MVLoad
CLASSPATH=$MV_HOME/lib/log4j-1.2.15.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mysql-connector-java-5.1.6.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/tomcat-jdbc-8.5.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/juli-6.0.53.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-api-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/guava-14.0.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/junit-4.11.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/hamcrest-core-1.3.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/commons-io-2.4.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mockito-all-1.9.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/servlet-api.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/commons-fileupload-1.3.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.8.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.8.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.8.2.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer_all.jar



if [ $# -eq 0 ]
then
  echo "Running plots scripts"
  $JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.test.AllTestRunner
else
  dir="$1"
  mode="$2"
  if [[ 1 -eq $# ]]
  then
    echo "Running plots scripts"
    $JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.test.AllTestRunner "$dir"

  else
    if [[ (2 -eq $#) &&  ($2 == "all") ]];
    then
      echo "Running all scripts"
      $JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.test.AllTestRunner "$dir"  all
    else
      if [[ (2 -eq $#) && ($2 == "plots") ]];
      then
        echo "Running plots scripts"
        $JAVA -classpath $CLASSPATH -Xmx2048M edu.ucar.metviewer.test.AllTestRunner
      fi
    fi
  fi
fi




