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
CLASSPATH="$MV_HOME/lib/*"
CLASSPATH=$CLASSPATH:$MV_HOME/dist/lib/metviewer.jar

PYTHON_ENV=<path_to_python_env>
METCALCPY_HOME=<path_to_metcalcpy_home>

java -classpath $CLASSPATH -Xmx4096M -Dpython.env=$PYTHON_ENV -Dmetcalcpy.home=$METCALCPY_HOME  edu.ucar.metviewer.scorecard.Scorecard $@
