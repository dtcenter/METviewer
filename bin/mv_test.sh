#!/bin/bash

# The METviewer test directory is set to something appropriate i.e. it has already been cloned and checked out to the right branch.
usage() { echo "Usage: $0 -t <path to METviewer test directory> [-m <path to METviewer home>] [-d <mv_database>] [-u <mv_user>] [-p mv_passwd] [-h <mv_host>] [-P <mv_port>] [-j <path to java executible>] [-c(capture created images)] [-n(no clean)] [-l(load data)] [-s(test servlet)]" 1>&2; exit 1; }
export MV_DATABASE="mv_test"
export MV_USER="mvuser"
export MV_PASSWD="mvuser"
export MV_HOST="dakota.rap.ucar.edu"
export MV_PORT=3306
export MV_TYPE="mariadb"
export NOCLEAN=""
export CAPTURE_CREATED_IMAGES=""
export LOADDATA=""
export TESTSERVLET=""
while getopts "t:m:d:u:p:P:k:h:j:cnls:e:a:r:g:?" o; do
    case "${o}" in
        t)
			if [ ! -d "${OPTARG}" ]; then
				echo "METviewer test directory ${OPTARG} does not exist"
				usage
			fi
            export MV_TEST_HOME=${OPTARG}
            ;;
        m)
			if [ ! -d "${OPTARG}" ]; then
				echo "METviewer directory ${OPTARG} does not exist"
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
        k)
            MV_TYPE=${OPTARG}
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
			   echo "You provided a bad java executable";
			   usage;
			fi
            ;;
        c)
            CAPTURE_CREATED_IMAGES="-DcaptureCreatedImages=yes"
            ;;
        n)
            NOCLEAN="-DnoClean=yes"
            ;;
        l)
            LOADDATA="yes"
            ;;
        s)
            TESTSERVLET="-DtestServlet=yes"
            ;;
        e)
            PYTHON_ENV_PAR="-Dpython.env=${OPTARG}"
            PYTHON_ENV="${OPTARG}"
            ;;
        a)
            METCALCPY_HOME="-Dmetcalcpy.env=${OPTARG}"
            ;;
        r)
            if [ ! -d "${OPTARG}" ]; then
				      echo "METPLOTPY_HOME directory ${OPTARG} does not exist"
				      OPTARG="/d3/projects/METViewer/METplotpy/metplotpy/"
			      fi
            METPLOTPY_HOME="-Dmetplotpy.env=${OPTARG}"
            ;;
        g)
            if [ ! -d "${OPTARG}" ]; then
              echo "METDATADB_HOME directory ${OPTARG} does not exist"
            	OPTARG="/d3/projects/METViewer/METdatadb/"
            fi
            METDATADB_HOME="-Dmetdatadb.env=${OPTARG}"
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
	echo "setting it to /d3/projects/METviewer/src_dev/apps/METviewer"
	MV_HOME=/d3/projects/METviewer/src_dev/apps/METviewer
else
	echo "MV_HOME is set to ${MV_HOME}"
fi


if [ -z ${MV_TEST_HOME+x} ]; then
	echo "MV_TEST_HOME is unset"
	echo "setting it to /d3/projects/METviewer/src_dev/apps/METviewer/test_data"
	MV_TEST_HOME=/d3/projects/METviewer/test_data
else
	echo "MV_TEST_HOME is set to ${MV_TEST_HOME}"
fi

if [ -z ${PYTHON_ENV+x} ]; then
	echo "PYTHON_ENV is unset"
	echo "setting it to /d3/projects/METViewer/METviewer_py3.6.3/"
	PYTHON_ENV=/d3/projects/METViewer/METviewer_py3.6.3/
else
	echo "PYTHON_ENV is set to ${PYTHON_ENV}"
fi

if [ -z ${METCALCPY_HOME+x} ]; then
	echo "METCALCPY_HOME is unset"
	echo "setting it to /d3/projects/METViewer/METcalcpy"
	METCALCPY_HOME=/d3/projects/METViewer/METcalcpy
else
	echo "METCALCPY_HOME is set to ${METCALCPY_HOME}"
fi

if [ -z ${METPLOTPY_HOME+x} ]; then
	echo "METPLOTPY_HOME is unset"
	echo "setting it to /d3/projects/METViewer/METplotpy/metplotpy/"
	METPLOTPY_HOME=/d3/projects/METViewer/METplotpy/metplotpy/
else
	echo "METPLOTPY_HOME is set to ${METPLOTPY_HOME}"
fi

if [ -z ${METDATADB_HOME+x} ]; then
	echo "METDATABD_HOME is unset"
	echo "setting it to /d3/projects/METViewer/METdatadb/"
	METDATADB_HOME=/d3/projects/METViewer/METdatadb/
else
	echo "METDATABD_HOME is set to ${METDATADB_HOME}"
fi

# construct the classpath
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
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.17.1.jar


echo "Running allRestRunner"

JAVA_OPTS="-Xmx2048M -ea -Dmv_root_dir=$MV_TEST_HOME -Dmv_database=$MV_DATABASE -Dmv_user=$MV_USER -Dmv_pwd=$MV_PASSWD -Dmv_host=$MV_HOST -Dmv_port=$MV_PORT -Dmv_type=$MV_TYPE -Dlog4j.configurationFile=file:${MV_HOME}/java/edu/ucar/metviewer/resources/log4j2.xml $CAPTURE_CREATED_IMAGES $NOCLEAN  $TESTSERVLET $PYTHON_ENV_PAR $METCALCPY_HOME $METPLOTPY_HOME $METDATADB_HOME"
echo "---------"
cd ${MV_HOME}

if [[$LOADDATA == "yes" ]]; then
  export PYTHONPATH=${PYTHONPATH}:$METDATADB_HOME
  echo "$PYTHON_ENV/bin/python  $METDATADB_HOME/METdbLoad/ush/met_db_load.py $MV_TEST_HOME/load_data/load/mv_mysql.sql"
  $PYTHON_ENV/bin/python  $METDATADB_HOME/METdbLoad/ush/met_db_load.py $MV_TEST_HOME/load_data/load/mv_mysql.sql
else
    echo "Skip data loading"
fi
echo $JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
$JAVA -classpath $CLASSPATH $JAVA_OPTS edu.ucar.metviewer.test.AllTestRunner
echo "---------"
ret=$?
echo "$0 result is $ret"
exit $ret