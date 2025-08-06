#!/bin/bash

# Necessary for generating the classpath
source all_jars_for_classpath.sh

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

# jar files needed for mv_compare.sh
#  commons-fileupload-1.4.jar
#  commons-io-2.8.0.jar
#  commons-lang3-3.11.jar
#  guava-14.0.1.jar
#  hamcrest-core-1.3.jar
#  jackson-core-2.12.0.jar
#  jackson-databind-2.12.0.jar
#  junit-4.11.jar
#  j2html-1.4.0.jar
#  log4j-api-2.17.1.jar
#  log4j-core-2.17.1.jar
#  log4j-iostreams-2.17.0.jar
#  mariadb-java-client-2.7.1.jar
#  metviewer_all.jar
#  mockito-all-1.9.5.jar
#  slf4j-api-1.7.5.jar
#  slf4j-log4j12-1.7.5.jar
#  snakeyaml-1.27.jar
#  tomcat-jdbc-8.5.61.jar
#  xercesImpl.jar
#  xml-apis.jar

echo "Running allRestRunner"
#/Users/pierce/test_data all
JAVA_OPTS="-Xmx2048M -ea  -DcompareOnly=yes  -Dmv_root_dir=$MV_TEST_HOME -Dmv_root_compare_dir=$MV_TEST_COMPARE_HOME -Dlog4j.configurationFile=file:${MV_HOME}/java/edu/ucar/metviewer/resources/log4j2.xml"
echo "---------"
cd $MV_HOME

# Construct the CLASSPATH from classpath
# categories defined in all_jars_for_classpath.sh
CLASSPATH=$CLASSPATH_COMMON:$CLASSPATH_TESTING:$CLASSPATH_SNAKE
echo -e "CLASSPATH for mv_compare: \n $CLASSPATH \n"


echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
$JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
echo "---------"
ret=$?
echo "$0 result is $ret"
exit $ret


