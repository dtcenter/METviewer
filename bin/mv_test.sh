#!/bin/bash

usage() { echo "Usage: $0 [-m <path to METViewer home>] [-t <path to METViewer test directory>] [-d <mv_database>] [-u <mv_user>] [-p mv_passwd] [-h <mv_host>] [-P <mv_port>] [-j <path to java executible>] [-c(capture created images)] [-n(no clean)] [-a(all)] [test_directory]" 1>&2; exit 1; }
export mv_database="mv_test"
export mv_user="mvuser"
export mv_pwd="mvuser"
export mv_host="dakota.rap.ucar.edu"
export mv_port=3306
export NOCLEAN=""
export CAPTURE_CREATED_IMAGES=""
all=""
while getopts "m:t:d:u:p:P:h:j:cnia?" o; do
    case "${o}" in
        m)
			if [ ! -d "${OPTARG}" ]; then
				echo "directory ${OPTARG} does not exist"
				usage
			fi
            export MV_HOME=${OPTARG}
            ;;
        t)
			if [ ! -d "${OPTARG}" ]; then
				echo "directory ${OPTARG} does not exist"
				usage
			fi
            export MV_TEST_HOME=${OPTARG}
            ;;
        d)
            MV_DATABASE=${OPTARG}
            ;;
        u)
            MV_USER=${OPTARG}
            ;;
        p)
            MV_PASSWD=${OPTARG}
            ;;
        P)
            MV_PORT=${OPTARG}
            ;;
        h)
            MV_HOST=${OPTARG}
            ;;
        j)
			if [ ! -x "${OPTARG}" ]; then
				echo "file ${OPTARG} does not exist or is not executible"
				usage
			fi
            export JAVA=${OPTARG}
			$JAVA -version
			if [ "$?" -ne "0" ]; then
			   echo "You provided a bad java executible";
			   exit 1;
			fi
            ;;
        c)
            CAPTURE_CREATED_IMAGES="-DcaptureCreatedImages=yes"
            ;;
        n)
            NOCLEAN="-DnoClean"
            ;;
        a)
            all=" all"
            ;;
		?) 
		usage
		;;
    esac
done
shift $((OPTIND-1))
echo "remaining params $*"

if [ -z ${JAVA+x} ]; then 
	JAVA=$(which java)
fi
$JAVA -version
if [ "$?" -ne "0" ]; then
   echo "You do not have a java executible in your path";
   exit 1;
fi

if [ -z ${MV_HOME+x} ]; then 
	echo "MV_HOME is unset"
	echo "setting it to /d3/projects/METViewer/src_dev/apps/METViewer"
	MV_HOME=/d3/projects/METViewer/src_dev/apps/METViewer
else
	echo "MV_HOME is set to ${MV_HOME}"
fi


if [ -z ${MV_TEST_HOME+x} ]; then 
	echo "MV_HOME is unset"
	echo "setting it to /d3/projects/METViewer/src_dev/apps/METViewer"
	MV_HOME=/d3/projects/METViewer/src_dev/apps/METViewer
else
	echo "MV_HOME is set to ${MV_HOME}"
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
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.10.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer_all.jar

#/Users/pierce/test_data all
JAVA_OPTS="-Xmx2048M -ea -Dmv_root_dir=$MV_TEST_HOME -Dmv_database=$MV_DATABASE -Dmv_user=$MV_USER -Dmv_pwd=$MV_PASSWD -Dmv_host=$MV_HOST -Dmv_port=$MV_PORT -Dlog4j.configurationFile=file:${MV_HOME}/java/edu/ucar/metviewer/resources/log4j2.xml $CAPTURE_CREATED_IMAGES $NOCLEAN"
#echo "JAVA_OPTS ARE ... $JAVA_OPTS"
echo "---------"

cd $MV_HOME
if [ $# -eq 0 ]
then
  echo "Running plots scripts - no params - $*"
    echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
    $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner $all
    ret=$?
else
  dir="$1"
  mode="$2"
  if [[ 1 -eq $# ]]
  then
    echo "Running plots scripts - 1 param - $*"
    echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner "$dir"
    $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner "$dir"
    ret=$?
  else
    if [[ (2 -eq $#) &&  ($2 == "all") ]]; then
      echo "Running all scripts - 2 params $dir \(all\) - $*"
      echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner "$dir" all
      $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner "$dir" all
      ret=$?
    else
      if [[ (2 -eq $#) && ($2 == "plots") ]]; then
        echo "Running plots scripts - 2 params \(plots\) - $*"
        echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner "$dir" plots
        $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunnery "$dir" plots
        ret=$?
      fi
    fi
  fi
fi
echo "$0 result is $ret"
exit $ret


