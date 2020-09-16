#!/bin/bash

#
# METVIEWER_DOCKER_IMAGE name
#
if [ -z ${METVIEWER_DOCKER_IMAGE+x} ]; then
  echo "ERROR:"
  echo "ERROR: METVIEWER_DOCKER_IMAGE defines the image to be run!"
  echo "ERROR: For example, 'dtcenter/metviewer' from DockerHub or 'metviewer' for a local build."
  echo "ERROR:"
  exit 1
else
  echo "METVIEWER_DOCKER_IMAGE = ${METVIEWER_DOCKER_IMAGE}"
fi

#
# MYSQL_DIR local path for MySQL data
#
if [ -z ${MYSQL_DIR+x} ]; then
  echo "ERROR:"
  echo "ERROR: MYSQL_DIR must be set to the local path for MySQL data!"
  echo "ERROR:"
  exit 1
else
  echo "MYSQL_DIR = ${MYSQL_DIR}"
fi

#
# METVIEWER_DATA local path for MET data to be loaded 
#
if [ -z ${METVIEWER_DATA+x} ]; then
  echo "ERROR:"
  echo "ERROR: METVIEWER_DATA must be set to the local path for MET data to be loaded!"
  echo "ERROR:"
  exit 1
else
  echo "METVIEWER_DATA = ${METVIEWER_DATA}"
fi

#
# METVIEWER_DIR local path for METviewer output
#
if [ -z ${METVIEWER_DIR+x} ]; then
  echo "ERROR:"
  echo "ERROR: METVIEWER_DIR must be set to the local path for METviewer output data!"
  echo "ERROR:"
  exit 1
else
  echo "METVIEWER_DIR = ${METVIEWER_DIR}"
fi

