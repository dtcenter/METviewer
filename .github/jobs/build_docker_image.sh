#!/bin/bash

source ${GITHUB_WORKSPACE}/.github/jobs/bash_functions.sh

DOCKERHUB_TAG=$(get_dockerhub_tag)

DOCKERFILE_PATH=${GITHUB_WORKSPACE}/internal/scripts/docker/Dockerfile.copy

CMD_LOGFILE=${GITHUB_WORKSPACE}/docker_build.log

echo "CURRENT ENVIRONMENT:"
printenv

time_command docker build -t ${DOCKERHUB_TAG} \
    --build-arg SOURCE_BRANCH \
    --build-arg MET_BASE_REPO \
    --build-arg MET_BASE_TAG \
    -f $DOCKERFILE_PATH ${GITHUB_WORKSPACE}
if [ $? != 0 ]; then
  cat ${CMD_LOGFILE}
  exit 1
fi

# Copy the dist directory from the image
id=$(docker create ${DOCKERHUB_TAG})
time_command docker cp $id:/METviewer/dist dist
docker rm -v $id
