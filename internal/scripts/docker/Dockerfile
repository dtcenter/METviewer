ARG MET_BASE_REPO=met-base-metviewer
ARG MET_BASE_TAG=v3.2

FROM dtcenter/${MET_BASE_REPO}:${MET_BASE_TAG}
MAINTAINER John Halley Gotway <johnhg@ucar.edu>

#
# This Dockerfile checks out METviewer and its dependencies from GitHub and builds
# the specified branch or tag. Use the develop branches for dependencies by default
# but override with "--build-arg".
#

ARG METVIEWER_GIT_NAME
ARG METPLOTPY_GIT_NAME=develop
ARG METCALCPY_GIT_NAME=develop
ARG METDATAIO_GIT_NAME=develop

#
# METVIEWER_GIT_NAME is required.
#
RUN if [ "x${METVIEWER_GIT_NAME}" = "x" ]; then \
      echo "ERROR: METVIEWER_GIT_NAME undefined! Rebuild with \"--build-arg METVIEWER_GIT_NAME={branch, tag, or hash}\""; \
      exit 1; \
    fi 

ENV METVIEWER_GIT_URL https://github.com/dtcenter/METviewer
ENV CATALINA_HOME /opt/tomcat

RUN echo "Build Argument METVIEWER_GIT_NAME=${METVIEWER_GIT_NAME}" \
 && echo "Build Argument METPLOTPY_GIT_NAME=${METPLOTPY_GIT_NAME}" \
 && echo "Build Argument METCALCPY_GIT_NAME=${METCALCPY_GIT_NAME}" \
 && echo "Build Argument METDATAIO_GIT_NAME=${METDATAIO_GIT_NAME}"

#
# Set env vars
#
ENV PYTHONPATH "${PYTHONPATH}:/METviewer-python/METcalcpy/:/METviewer-python/METplotpy/"
ENV METPLOTPY_BASE "/METviewer-python/METplotpy/"

#
# Clone the METviewer repository
#
RUN echo "Checking out METviewer ${METVIEWER_GIT_NAME} from ${METVIEWER_GIT_URL}"
RUN git clone --branch ${METVIEWER_GIT_NAME} ${METVIEWER_GIT_URL} /METviewer

#
# Run the build script
#
RUN cd /METviewer \
 && internal/scripts/docker/build_metviewer_docker.sh

ENTRYPOINT ${CATALINA_HOME}/bin/startup.sh && /bin/bash
CMD ["true"]
