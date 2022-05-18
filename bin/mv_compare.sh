#!/bin/bash

# The METViewer test directory is set to something appropriate i.e. it has already been cloned and checked out to the right branch.
usage() { echo "Usage: $0 -t <path to METViewer test directory> -m <path to METViewer home> -c <path to METViewer test compare directory>" 1>&2; exit 1; }
while getopts "t:c:m:j:?" o; do
    case "${o}" in
        t)
			if [ ! -d "${OPTARG}" ]; then
				echo "METViewer test directory ${OPTARG} does not exist"
				usage
			fi
            export MV_TEST_HOME=${OPTARG}
            ;;
        c)
			if [ ! -d "${OPTARG}" ]; then
				echo "METViewer test compare directory ${OPTARG} does not exist"
				usage
			fi
            export MV_TEST_COMPARE_HOME=${OPTARG}
            ;;
        m)
			if [ ! -d "${OPTARG}" ]; then
				echo "METViewer directory ${OPTARG} does not exist"
				usage
			fi
            export MV_HOME=${OPTARG}
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
	usage
fi
if [ -z ${MV_TEST_HOME+x} ]; then
	echo "MV_TEST_HOME is unset"
	usage
fi

if [ -z ${MV_TEST_COMPARE_HOME+x} ]; then
	echo "MV_TEST_COMPARE_HOME is unset"
	usage
fi

# construct the classpath for MVLoad
CLASSPATH=${MV_HOME}/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/juli-6.0.53.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/slf4j-api-1.7.5.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/guava-14.0.1.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/junit-4.11.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/hamcrest-core-1.3.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/commons-io-2.8.0.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/mockito-all-1.9.5.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/servlet-api-4.0.1.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/lib/commons-fileupload-1.4.jar
CLASSPATH=$CLASSPATH:${MV_HOME}/dist/lib/metviewer_all.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/j2html-1.4.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/jackson-core-2.12.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/jackson-databind-2.12.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/commons-lang3-3.11.jar

CLASSPATH=$CLASSPATH:$MV_HOME/lib/mariadb-java-client-2.7.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/tomcat-jdbc-8.5.61.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.17.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.17.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.17.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/snakeyaml-1.27.jar

echo "Running allRestRunner"
#/Users/pierce/test_data all
JAVA_OPTS="-Xmx2048M -ea  -DcompareOnly=yes  -Dmv_root_dir=$MV_TEST_HOME -Dmv_root_compare_dir=$MV_TEST_COMPARE_HOME -Dlog4j.configurationFile=file:${MV_HOME}/java/edu/ucar/metviewer/resources/log4j2.xml"
echo "---------"
cd $MV_HOME
echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
$JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
echo "---------"
ret=$?
echo "$0 result is $ret"
exit $ret


