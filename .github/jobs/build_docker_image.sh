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

time_command docker build -t ${DOCKERHUB_TAG} \
    --build-arg METVIEWER_GIT_NAME=${SOURCE_BRANCH} \
    --build-arg SOURCE_BRANCH \
    --build-arg MET_BASE_REPO \
    --build-arg MET_BASE_TAG \
    -f $DOCKERFILE_PATH ${GITHUB_WORKSPACE}
if [ $? != 0 ]; then
  cat ${CMD_LOGFILE}
  exit 1
fi

# Copy the build directory from the image
id=$(docker create ${DOCKERHUB_TAG})
time_command docker cp $id:/METviewer/build build 
docker rm -v $id
