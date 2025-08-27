#!/bin/bash
# get the METviewer home directory. It should be one level up from the current dir
# or if the link was used - the original directory

SOURCE="${BASH_SOURCE[0]}"

while [ -h "$SOURCE" ]; do
  # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

MV_HOME="$(dirname "$DIR")"

# construct the classpath for MVBatch
CLASSPATH=$CLASSPATH:$MV_HOME/lib/mariadb-java-client-2.7.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xercesImpl.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/xml-apis.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/tomcat-jdbc-8.5.61.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/juli-6.0.53.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-api-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/slf4j-log4j12-1.7.5.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/guava-33.4.8.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-api-2.17.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-core-2.17.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/log4j-iostreams-2.17.1.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/servlet-api.jar
CLASSPATH=$CLASSPATH:$MV_HOME/lib/snakeyaml-2.0.jar
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer.jar

PYTHON_ENV=<path-to-python-env>
METCALCPY_HOME=<path-to-metcalcpy>
METPLOTPY_HOME=<path-to-metplotpy>

echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
echo -e "Chrome errors will be generated as a result of invoking Python within the Java code.
\nThis is due to changes in Plotly and its use of Chrome in saving static images.
\nPlotting will continue via another route. "
echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n"

#java -classpath $CLASSPATH -Xmx2048M -Dpython.env=$PYTHON_ENV -Dmetcalcpy.home=$METCALCPY_HOME -Dmetplotpy.home=$METPLOTPY_HOME edu.ucar.metviewer.MVBatch $@


CWD=`pwd`
BATCH_LOC=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
PYTHON_BINARY="$PYTHON_ENV/bin/python"
export PYTHONPATH="$METCALCPY_HOME:$METCALCPY_HOME/metcalcpy:$METPLOTPY_HOME:$METPLOTPY_HOME/metplotpy:$METPLOTPY_HOME/metplotpy/plots"

echo -e "Generating Plots Directly with Python...\n"
$PYTHON_BINARY  $BATCH_LOC/make_batch_plots.py --xml $1 --loc $BATCH_LOC --pyenv $PYTHON_BINARY  --plotpy $METPLOTPY_HOME 





