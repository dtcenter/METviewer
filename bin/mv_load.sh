#!/bin/bash

PYTHON_ENV=<path_to_python_env>
METDATAIO_HOME=<path_to_metdataio_home>

export PYTHONPATH=${PYTHONPATH}:$METDATAIO_HOME

$PYTHON_ENV/bin/python  $METDATAIO_HOME/METdbLoad/ush/met_db_load.py $@
CODE=$?
if [ $CODE -ne 0 ]
then
    echo "Process finished with exit code $CODE"
fi
exit $CODE
