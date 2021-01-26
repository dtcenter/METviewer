#!/bin/bash

PYTHON_ENV=/Volumes/d1/tatiana/miniconda3/envs/METdatadb/
METDATADB_HOME=/Users/tatiana/PycharmProjects/METdatadb

export PYTHONPATH=${PYTHONPATH}:$METDATADB_HOME

$PYTHON_ENV/bin/python  $METDATADB_HOME/METdbLoad/ush/met_db_load.py $@
