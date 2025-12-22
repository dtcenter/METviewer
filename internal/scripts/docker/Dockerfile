ARG MET_BASE_REPO=met-base-metviewer-dev
ARG MET_BASE_TAG=feature_52_v3.4_ib_metviewer

FROM dtcenter/${MET_BASE_REPO}:${MET_BASE_TAG}
LABEL org.opencontainers.image.authors="John Halley Gotway <johnhg@ucar.edu>"


#
# This Dockerfile checks out METviewer and its dependencies from GitHub and builds
# the specified branch or tag. Use the develop branches for dependencies by default
# but override with "--build-arg".
#
# METVIEWER_GIT_NAME is required.
ARG SOURCE_BRANCH=develop
ARG METPLOTPY_GIT_NAME=develop
ARG METCALCPY_GIT_NAME=develop
ARG METDATAIO_GIT_NAME=develop

#
# SOURCE_BRANCH is required.
#
RUN if [ "x${SOURCE_BRANCH}" = "x" ]; then \
      echo "ERROR: SOURCE_BRANCH undefined! Rebuild with \"--build-arg SOURCE_BRANCH={branch, tag, or hash}\""; \
      exit 1; \
    else \
      echo "Build Argument SOURCE_BRANCH=${SOURCE_BRANCH}"; \
    fi

ENV METVIEWER_GIT_URL=https://github.com/dtcenter/METviewer
ENV METVIEWER_GIT_NAME=${SOURCE_BRANCH}
ENV CATALINA_HOME=/opt/tomcat

RUN echo "Build Argument METVIEWER_GIT_NAME=${METVIEWER_GIT_NAME}" \
 && echo "Build Argument METPLOTPY_GIT_NAME=${METPLOTPY_GIT_NAME}" \
 && echo "Build Argument METCALCPY_GIT_NAME=${METCALCPY_GIT_NAME}" \
 && echo "Build Argument METDATAIO_GIT_NAME=${METDATAIO_GIT_NAME}"

#
# Set env vars
#
ENV PYTHONPATH="/METviewer-python/METcalcpy/:/METviewer-python/METplotpy/"
ENV METPLOTPY_BASE="/METviewer-python/METplotpy/"

#
# Update the OS, as needed
#
RUN apt-get update && apt-get -y upgrade

#
# Clone the METviewer repository
#
RUN echo "Checking out METviewer ${METVIEWER_GIT_NAME} from ${METVIEWER_GIT_URL}"
RUN git clone --branch ${METVIEWER_GIT_NAME} ${METVIEWER_GIT_URL} /METviewer

#
# Configure METviewer
#
RUN echo "Configuring METviewer" \
 && cd /METviewer \
 && cat webapp/metviewer/WEB-INF/classes/build.properties | \
    sed -r 's%db.host=.*%db.host=mysql_mv%g' | \
    sed -r 's%db.user=.*%db.user=root%g' | \
    sed -r 's%db.password=.*%db.password=mvuser%g' | \
    sed -r 's%db.management.system=.*%db.management.system=mysql%g' | \
    sed -r 's%output.dir=.*%output.dir=/opt/tomcat/webapps/metviewer_output/%g' | \
    sed -r 's%webapps.dir=.*%webapps.dir=/opt/tomcat/webapps/metviewer/%g' | \
    sed -r 's%url.output=.*%url.output=http://localhost:8080/metviewer_output/%g' | \
    sed -r 's%python.env=.*%python.env=/usr/local%g' | \
    sed -r 's%metcalcpy.home=.*%metcalcpy.home=/METviewer-python/METcalcpy/%g' | \
    sed -r 's%metplotpy.home=.*%metplotpy.home=/METviewer-python/METplotpy/%g' \
    > build.properties

#
# Run the build script
#
RUN cd /METviewer  && internal/scripts/docker/build_metviewer_docker.sh

# Explicitly specify the shell to indicate that using the SHELL
# instruction is a conscious decision: 
# https://docs.docker.com/reference/build-checks/json-args-recommended/
SHELL ["/bin/sh", "-c"]
ENTRYPOINT ${CATALINA_HOME}/bin/startup.sh && /bin/bash
CMD ["true"]

# Set the PRE_LOAD_CHROME env variable to True to prevent invoking get_chrome_sync() from METplotpy
ENV PRE_LOAD_CHROME='True'
