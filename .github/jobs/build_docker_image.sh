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

# Get METdataio version
# use override version if specified, otherwise try to determine version

if [ -n "${METDATAIO_OVERRIDE}" ]; then
  METDATAIO_VERSION=${METDATAIO_OVERRIDE}
else
  METDATAIO_VERSION=$(${GITHUB_WORKSPACE}/METplus/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METdataio ${component_args})
fi

# if override is not set and cannot get version from script,
# error if development versions are not allowed, otherwise use develop

if [ -z "${METDATAIO_VERSION}" ]; then

  echo "ERROR: Could not get METdataio version from ${SOURCE_BRANCH}"
  exit 1

fi

echo "Using METdataio ${METDATAIO_VERSION}"

# get METcalcpy version

if [ -n "${METCALCPY_OVERRIDE}" ]; then
  METCALCPY_VERSION=${METCALCPY_OVERRIDE}
else
  METCALCPY_VERSION=$(${GITHUB_WORKSPACE}/METplus/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METcalcpy ${component_args})
fi

if [ -z "${METCALCPY_VERSION}" ]; then

  echo "ERROR: Could not get METcalcpy version from ${SOURCE_BRANCH}"
  exit 1

fi

echo "Using METcalcpy ${METCALCPY_VERSION}"

# get METplotpy version

if [ -n "${METPLOTPY_OVERRIDE}" ]; then
  METPLOTPY_VERSION=${METPLOTPY_OVERRIDE}
else
  METPLOTPY_VERSION=$(${GITHUB_WORKSPACE}/METplus/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METplotpy)
fi

if [ -z "${METPLOTPY_VERSION}" ]; then

  echo "ERROR: Could not get METplotpy version from ${SOURCE_BRANCH}"
  exit 1
 
fi

echo "Using METplotpy ${METPLOTPY_VERSION}"

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

# Copy the build directory from the image
id=$(docker create ${DOCKERHUB_TAG})
time_command docker cp $id:/METviewer/build build 
docker rm -v $id
