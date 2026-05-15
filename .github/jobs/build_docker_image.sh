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


# if USE_DEVELOPMENT is "true", then get main_vX.Y or develop, otherwise get vX.Y.Z
component_args=""
if [[ "${USE_DEVELOPMENT}" == "true" ]]; then
  component_args="-f main_v{X}.{Y}"
fi

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

  if [[ "${USE_DEVELOPMENT}" != "true" ]]; then
    echo "ERROR: Could not get METdataio version from ${SOURCE_BRANCH}"
    exit 1
  fi

  METDATAIO_VERSION=develop

fi

echo "Using METdataio ${METDATAIO_VERSION}"

# get METcalcpy version

if [ -n "${METCALCPY_OVERRIDE}" ]; then
  METCALCPY_VERSION=${METCALCPY_OVERRIDE}
else
  METCALCPY_VERSION=$(${GITHUB_WORKSPACE}/METplus/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METcalcpy ${component_args})
fi

if [ -z "${METCALCPY_VERSION}" ]; then

  if [[ "${USE_DEVELOPMENT}" != "true" ]]; then
    echo "ERROR: Could not get METcalcpy version from ${SOURCE_BRANCH}"
    exit 1
  fi

  METCALCPY_VERSION=develop

fi

echo "Using METcalcpy ${METCALCPY_VERSION}"

# get METplotpy version

if [ -n "${METPLOTPY_OVERRIDE}" ]; then
  METPLOTPY_VERSION=${METPLOTPY_OVERRIDE}
else
  METPLOTPY_VERSION=$(${GITHUB_WORKSPACE}/METplus/metplus/component_versions.py -i METviewer -v "${SOURCE_BRANCH}" -o METplotpy ${component_args})
fi

if [ -z "${METPLOTPY_VERSION}" ]; then

  if [[ "${USE_DEVELOPMENT}" != "true" ]]; then
    echo "ERROR: Could not get METplotpy version from ${SOURCE_BRANCH}"
    exit 1
  fi

  METPLOTPY_VERSION=develop

fi

echo "Using METplotpy ${METPLOTPY_VERSION}"

time_command docker build -t ${DOCKERHUB_TAG} \
    --build-arg METVIEWER_GIT_NAME=${SOURCE_BRANCH} \
    --build-arg SOURCE_BRANCH \
    --build-arg MET_BASE_REPO \
    --build-arg MET_BASE_TAG \
    --build-arg BASE_REGISTRY=${REGISTRY} \
    --build-arg BASE_IMAGE=${RHEL_IMAGE} \
    --build-arg METDATAIO_GIT_NAME=${METDATAIO_VERSION} \
    --build-arg METCALCPY_GIT_NAME=${METCALCPY_VERSION} \
    --build-arg METPLOTPY_GIT_NAME=${METPLOTPY_VERSION} \
    -f $DOCKERFILE_PATH ${GITHUB_WORKSPACE}
if [ $? != 0 ]; then
  cat ${CMD_LOGFILE}
  exit 1
fi
