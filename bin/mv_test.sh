#!/bin/bash

# The METViewer test directory is set to something appropriate i.e. it has already been cloned and checked out to the right branch.
usage() { echo "Usage: $0 -t <path to METViewer test directory> [-m <path to METViewer home>] [-d <mv_database>] [-u <mv_user>] [-p mv_passwd] [-h <mv_host>] [-P <mv_port>] [-j <path to java executible>] [-c(capture created images)] [-n(no clean)] [-l(load data)]" 1>&2; exit 1; }
export MV_DATABASE="mv_test"
export MV_USER="mvuser"
export MV_PASSWD="mvuser"
export MV_HOST="dakota.rap.ucar.edu"
export MV_PORT=3306
export NOCLEAN=""
export CAPTURE_CREATED_IMAGES=""
export LOADDATA=""
while getopts "t:m:d:u:p:P:h:j:cnl?" o; do
    case "${o}" in
        t)
			if [ ! -d "${OPTARG}" ]; then
				echo "METViewer test directory ${OPTARG} does not exist"
				usage
			fi
            export MV_TEST_HOME=${OPTARG}
            ;;
        m)
			if [ ! -d "${OPTARG}" ]; then
				echo "METViewer directory ${OPTARG} does not exist"
				usage
			fi
            export MV_HOME=${OPTARG}
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
			   usage;
			fi
            ;;
        c)
            CAPTURE_CREATED_IMAGES="-DcaptureCreatedImages=yes"
            ;;
        n)
            NOCLEAN="-DnoClean"
            ;;
        l)
            LOADDATA="-DloadData"
            ;;
		?)
		usage
		;;
    esac
done
shift $((OPTIND-1))

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
	echo "MV_TEST_HOME is unset"
	echo "setting it to /d3/projects/METViewer/src_dev/apps/METViewer/test_data"
	MV_TEST_HOME=/d3/projects/METViewer/test_data
else
	echo "MV_TEST_HOME is set to ${MV_TEST_HOME}"
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

echo "Running allRestRunner"
#/Users/pierce/test_data all
JAVA_OPTS="-Xmx2048M -ea -Dmv_root_dir=$MV_TEST_HOME -Dmv_database=$MV_DATABASE -Dmv_user=$MV_USER -Dmv_pwd=$MV_PASSWD -Dmv_host=$MV_HOST -Dmv_port=$MV_PORT -Dlog4j.configurationFile=file:${MV_HOME}/java/edu/ucar/metviewer/resources/log4j2.xml $CAPTURE_CREATED_IMAGES $NOCLEAN $LOADDATA"
echo "---------"
cd $MV_HOME
echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
$JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
echo "---------"
ret=$?
echo "$0 result is $ret"
exit $ret