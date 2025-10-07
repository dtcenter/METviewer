#!/bin/bash

source ${GITHUB_WORKSPACE}/.github/jobs/bash_functions.sh

DOCKERHUB_TAG=$(get_dockerhub_tag)

# For the release-docker-images.yaml workflow
DOCKERFILE_PATH=${GITHUB_WORKSPACE}/${SOURCE_BRANCH}/internal/scripts/docker/Dockerfile

# For the sonarqube.yaml workflow
if [[ ! -f "${DOCKERFILE_PATH}" ]]; then
  DOCKERFILE_PATH=${GITHUB_WORKSPACE}/internal/scripts/docker/Dockerfile.copy
fi

CMD_LOGFILE=${GITHUB_WORKSPACE}/docker_build.log

# Get METplus Analysis tool versions
METDATAIO_VERSION=$(${GITHUB_WORKSPACE}/develop/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METdataio)
METCALCPY_VERSION=$(${GITHUB_WORKSPACE}/develop/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METcalcpy)
METPLOTPY_VERSION=$(${GITHUB_WORKSPACE}/develop/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METplotpy)

if [ -z "${METDATAIO_VERSION}" ]; then
  echo "ERROR: Could not get METdataio version from ${SOURCE_BRANCH}"
  exit 1
fi

if [ -z "${METCALCPY_VERSION}" ]; then
  echo "ERROR: Could not get METcalcpy version from ${SOURCE_BRANCH}"
  exit 1
fi

if [ -z "${METPLOTPY_VERSION}" ]; then
  echo "ERROR: Could not get METplotpy version from ${SOURCE_BRANCH}"
  exit 1
fi

time_command docker build -t ${DOCKERHUB_TAG} \
    --build-arg METVIEWER_GIT_NAME=${SOURCE_BRANCH} \
    --build-arg SOURCE_BRANCH \
    --build-arg MET_BASE_REPO \
    --build-arg MET_BASE_TAG \
    --build-arg METDATAIO_GIT_NAME=${METDATAIO_VERSION} \
    --build-arg METCALCPY_GIT_NAME=${METCALCPY_VERSION} \
    --build-arg METPLOTPY_GIT_NAME=${METPLOTPY_VERSION} \
    -f $DOCKERFILE_PATH ${GITHUB_WORKSPACE}
if [ $? != 0 ]; then
  cat ${CMD_LOGFILE}
  exit 1
fi
