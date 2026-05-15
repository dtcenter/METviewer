ARG MET_BASE_REPO=met-base-metviewer-dev
ARG MET_BASE_TAG=feature_74_ubi9

# base image that files will be copied into
ARG BASE_REGISTRY=docker.io
ARG BASE_IMAGE=redhat/ubi9
ARG BASE_TAG=9.7

FROM dtcenter/${MET_BASE_REPO}:${MET_BASE_TAG} AS build
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

ENV METVIEWER_GIT_URL=https://github.com/dtcenter/METviewer
ENV METVIEWER_GIT_NAME=${SOURCE_BRANCH}
ENV CATALINA_HOME=/opt/tomcat
ENV PYTHONPATH="/METviewer-python/METcalcpy/:/METviewer-python/METplotpy/"
ENV METPLOTPY_BASE="/METviewer-python/METplotpy/"

# Update the OS, as needed
# Clone the METviewer repository

RUN echo "Build Argument METVIEWER_GIT_NAME=${METVIEWER_GIT_NAME}" \
 && echo "Build Argument METPLOTPY_GIT_NAME=${METPLOTPY_GIT_NAME}" \
 && echo "Build Argument METCALCPY_GIT_NAME=${METCALCPY_GIT_NAME}" \
 && echo "Build Argument METDATAIO_GIT_NAME=${METDATAIO_GIT_NAME}" \
 && echo "Checking out METviewer ${METVIEWER_GIT_NAME} from ${METVIEWER_GIT_URL}" \
 && git clone --branch ${METVIEWER_GIT_NAME} ${METVIEWER_GIT_URL} /METviewer

WORKDIR /METviewer

# Configure METviewer
# Run the build script

RUN echo "Configuring METviewer" \
 && cat webapp/metviewer/WEB-INF/classes/build.properties | \
    sed -r 's%db.host=.*%db.host=mysql_mv%g' | \
    sed -r 's%db.user=.*%db.user=root%g' | \
    sed -r 's%db.password=.*%db.password=mvuser%g' | \
    sed -r 's%db.management.system=.*%db.management.system=mariadb%g' | \
    sed -r 's%output.dir=.*%output.dir=/opt/tomcat/webapps/metviewer_output/%g' | \
    sed -r 's%webapps.dir=.*%webapps.dir=/opt/tomcat/webapps/metviewer/%g' | \
    sed -r 's%url.output=.*%url.output=http://localhost:8080/metviewer_output/%g' | \
    sed -r 's%python.env=.*%python.env=/usr/local%g' | \
    sed -r 's%metcalcpy.home=.*%metcalcpy.home=/METviewer-python/METcalcpy/%g' | \
    sed -r 's%metplotpy.home=.*%metplotpy.home=/METviewer-python/METplotpy/%g' \
    > build.properties \
 && echo "Run build_metviewer_docker.sh script" &&\
    internal/scripts/docker/build_metviewer_docker.sh

# copy only files needed to run METviewer into the final image
FROM ${BASE_REGISTRY}/${BASE_IMAGE}:${BASE_TAG}

ENV PYTHON_XY=3.14

# METviewer code
COPY --from=build /METviewer /METviewer
COPY --from=build /METviewer-python /METviewer-python

# Tomcat
COPY --from=build /opt/tomcat /opt/tomcat

COPY --from=build /usr/lib64/*so* /usr/lib64/

# Database (MariaDB)
COPY --from=build /var/lib/mysql /var/lib/mysql
COPY --from=build /usr/lib64/mariadb /usr/lib64/mariadb
COPY --from=build /usr/bin/mysql* /usr/bin/
COPY --from=build /usr/bin/mariadb* /usr/bin/
COPY --from=build /usr/bin/my_print_defaults /usr/bin/
COPY --from=build /usr/share/mariadb /usr/share/mariadb
COPY --from=build /usr/sbin/mariadbd /usr/sbin/mariadbd
COPY --from=build /usr/bin/resolveip /usr/bin/resolveip

# Python
COPY --from=build /usr/local/bin/python* /usr/local/bin/
COPY --from=build /usr/local/lib/python${PYTHON_XY} /usr/local/lib/python${PYTHON_XY}
COPY --from=build /usr/local/lib/libpython3* /usr/local/lib/

# copy geos dynamic libs
COPY --from=build /usr/local/lib/libgeos*.so* /usr/local/lib/

# Java
COPY --from=build /usr/lib/jvm /usr/lib/jvm
COPY --from=build /etc/java /etc/java

# R
#COPY --from=build /etc/R /etc/R
# JUST /usr/include/R?
COPY --from=build /usr/include /usr/include
COPY --from=build /usr/lib64/R /usr/lib64/R
COPY --from=build /usr/bin/R /usr/bin/R
COPY --from=build /usr/bin/Rscript /usr/bin/Rscript

# R packages
COPY --from=build /usr/lib64/R/library /usr/lib64/R/library

# R fonts
COPY --from=build /etc/fonts /etc/fonts
COPY --from=build /usr/share/fontconfig /usr/share/fontconfig
COPY --from=build /usr/share/fonts /usr/share/fonts

# ghostscript (needed by R)
COPY --from=build /usr/bin/gs /usr/bin/gs
COPY --from=build /usr/bin/ghostscript /usr/bin/ghostscript
COPY --from=build /usr/share/ghostscript /usr/share/ghostscript
COPY --from=build /usr/share/poppler /usr/share/poppler

# increase proxy timeout
# create mysql user
RUN \
    sed -i 's|connectionTimeout=".*"|connectionTimeout="40000"|g' /opt/tomcat/conf/server.xml \
 && groupadd -r -g 1002 mysql && \
    useradd -r -u 1002 -m -g 1002 mysql

# set environment variables need to run METviewer
ENV CATALINA_HOME=/opt/tomcat
ENV PYTHONPATH="/METviewer-python/METcalcpy/:/METviewer-python/METplotpy/"
ENV METPLOTPY_BASE="/METviewer-python/METplotpy/"
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Explicitly specify the shell to indicate that using the SHELL
# instruction is a conscious decision: 
# https://docs.docker.com/reference/build-checks/json-args-recommended/
SHELL ["/bin/sh", "-c"]
ENTRYPOINT ${CATALINA_HOME}/bin/startup.sh && /bin/bash
CMD ["true"]
