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

time_command docker build -t ${DOCKERHUB_TAG} \
    --build-arg METVIEWER_GIT_NAME=${SOURCE_BRANCH} \
    --build-arg SOURCE_BRANCH \
    --build-arg MET_BASE_REPO \
    --build-arg MET_BASE_TAG \
    --build-arg METDATAIO_VERSION=${METDATAIO_VERSION} \
    --build-arg METCALCPY_VERSION=${METCALCPY_VERSION} \
    --build-arg METPLOTPY_VERSION=${METPLOTPY_VERSION} \
    -f $DOCKERFILE_PATH ${GITHUB_WORKSPACE}
if [ $? != 0 ]; then
  cat ${CMD_LOGFILE}
  exit 1
fi
