ARG MET_BASE_REPO=met-base-metviewer
ARG MET_BASE_TAG=v3.3

FROM dtcenter/${MET_BASE_REPO}:${MET_BASE_TAG}
LABEL org.opencontainers.image.authors="John Halley Gotway <johnhg@ucar.edu>" 

#
# This Dockerfile checks out METviewer and its dependencies from GitHub and builds
# the specified branch or tag. Use the develop branches for dependencies by default
# but override with "--build-arg".
#

ARG METVIEWER_GIT_NAME=feature_585_update_jars
ARG METPLOTPY_GIT_NAME=main_v3.0
ARG METCALCPY_GIT_NAME=main_v3.0
ARG METDATAIO_GIT_NAME=main_v3.0


#
# METVIEWER_GIT_NAME is required.
#
RUN if [ "x${METVIEWER_GIT_NAME}" = "x" ]; then \
      echo "ERROR: METVIEWER_GIT_NAME undefined! Rebuild with \"--build-arg METVIEWER_GIT_NAME={branch, tag, or hash}\""; \
      exit 1; \
    fi 

ENV METVIEWER_GIT_URL=https://github.com/dtcenter/METviewer
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
RUN apt update && apt -y upgrade

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
    sed -r 's%python.env=.*%python.env=/usr/local/%g' | \
    sed -r 's%metcalcpy.home=.*%metcalcpy.home=/METviewer-python/METcalcpy/%g' | \
    sed -r 's%metplotpy.home=.*%metplotpy.home=/METviewer-python/METplotpy/%g' \
    > build.properties

#
#  Ensure compatible versions of numpy and pandas are installed
#
RUN python3 -m pip install --upgrade pip
RUN pip install numpy==1.24.2 pandas==1.5.2

#
# Run the build script
#
RUN cd /METviewer \
 && internal/scripts/docker/build_metviewer_docker.sh

SHELL ["/bin/sh", "-c"]
ENTRYPOINT ${CATALINA_HOME}/bin/startup.sh && /bin/bash
CMD ["true"]
