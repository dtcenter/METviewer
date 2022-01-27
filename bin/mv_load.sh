#!/bin/bash

PYTHON_ENV=<path_to_python_env>
METDATADB_HOME=<path_to_metdatadb_home>

export PYTHONPATH=${PYTHONPATH}:$METDATADB_HOME

$PYTHON_ENV/bin/python  $METDATADB_HOME/METdbLoad/ush/met_db_load.py $@
