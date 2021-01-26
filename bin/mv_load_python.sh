#!/bin/bash

PYTHON_ENV=<path_to_python_env>
METDATADB_HOME=<path_to_metdatadb_home>

$PYTHON_ENV  $METDATADB_HOME/METdbLoad/ush/met_db_load.py $@
