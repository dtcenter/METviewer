ARG MET_BASE_REPO=met-base-metviewer
ARG MET_BASE_TAG=3.4-latest

# base image that files will be copied into
ARG BASE_REGISTRY=docker.io
ARG BASE_IMAGE=debian
ARG BASE_TAG=12-slim

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


WORKDIR /METviewer

#
# Configure METviewer
#
RUN echo "Configuring METviewer" \
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
RUN internal/scripts/docker/build_metviewer_docker.sh

# copy only files needed to run METviewer into the final image
FROM ${BASE_REGISTRY}/${BASE_IMAGE}:${BASE_TAG}

# METviewer code
COPY --from=build /METviewer /METviewer
COPY --from=build /METviewer-python /METviewer-python

# Tomcat
COPY --from=build /opt/tomcat /opt/tomcat

COPY --from=build /usr/lib/x86_64-linux-gnu/*so* /usr/lib/x86_64-linux-gnu/

# created in base image -- look for packages in /usr/local
COPY --from=build /etc/ld.so.conf.d/usr-local.conf /etc/ld.so.conf.d/usr-local.conf

# Database (MariaDB)
COPY --from=build /var/lib/mysql /var/lib/mysql
COPY --from=build /usr/lib/mysql /usr/lib/mysql
COPY --from=build /usr/bin/mysql* /usr/bin/
COPY --from=build /usr/bin/mariadb* /usr/bin/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libedit.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libncurses.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libbsd.so* /usr/lib/x86_64-linux-gnu/

# Python
COPY --from=build /usr/local/bin/python* /usr/local/bin/
COPY --from=build /usr/local/lib/python3.12 /usr/local/lib/python3.12
COPY --from=build /usr/local/lib/libpython3* /usr/local/lib/

# copy geos and sqlite3 dynamic libs
COPY --from=build /usr/local/lib/*.so* /usr/local/lib/

# chrome (needed by plotly/kaleido python packages)
#COPY --from=build /usr/bin/google-chrome* /usr/bin/
COPY --from=build /opt/google/chrome /opt/google/chrome
COPY --from=build /usr/local/bin/choreo_diagnose /usr/local/bin/choreo_diagnose

# copy package to reinstall to get all missing dependencies
COPY --from=build /tmp/google-chrome-stable_current_amd64.deb /tmp/google-chrome-stable_current_amd64.deb
#COPY --from=build /usr/lib/x86_64-linux-gnu/libnss*so* /usr/lib/x86_64-linux-gnu/

#RUN ln -s /opt/google/chrome/google-chrome /usr/bin/google-chrome-stable &&\
#    ln -s /usr/bin/google-chrome-stable /usr/bin/google-chrome

# Java
COPY --from=build /usr/lib/jvm /usr/lib/jvm
COPY --from=build /etc/java-17-openjdk /etc/java-17-openjdk

# R
COPY --from=build /etc/R /etc/R
COPY --from=build /usr/include /usr/include
COPY --from=build /usr/lib/R /usr/lib/R
COPY --from=build /usr/bin/R /usr/bin/R
COPY --from=build /usr/bin/Rscript /usr/bin/Rscript
#COPY --from=build /usr/lib/x86_64-linux-gnu/libblas.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libreadline.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libtirpc.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libicuuc.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libicui18n.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libgomp.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libgssapi_krb5.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libicudata.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libkrb5.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libk5crypto.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libkrb5support.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libkeyutils.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/liblapack.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libgfortran.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libquadmath.so* /usr/lib/x86_64-linux-gnu/

# R packages
COPY --from=build /usr/local/lib/R/site-library /usr/local/lib/R/site-library

# R fonts
COPY --from=build /etc/fonts /etc/fonts
COPY --from=build /usr/share/fontconfig /usr/share/fontconfig
COPY --from=build /usr/share/fonts /usr/share/fonts

# ghostscript (needed by R)
COPY --from=build /usr/bin/gs /usr/bin/gs
COPY --from=build /usr/bin/ghostscript /usr/bin/ghostscript
COPY --from=build /usr/share/ghostscript /usr/share/ghostscript
COPY --from=build /var/lib/ghostscript /var/lib/ghostscript
COPY --from=build /etc/ghostscript /etc/ghostscript
COPY --from=build /usr/share/poppler /usr/share/poppler
COPY --from=build /usr/share/color /usr/share/color
#COPY --from=build /usr/lib/x86_64-linux-gnu/libgs.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libXt.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libX11.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libtiff.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libcups.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libijs-0.35.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libpng16.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libjbig2dec.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libjpeg.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/liblcms2.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libidn.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libpaper.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libfontconfig.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libfreetype.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libopenjp2.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libSM.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libICE.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libxcb.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libwebp.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libLerc.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libjbig.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libdeflate.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libavahi-common.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libavahi-client.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libexpat.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libbrotlidec.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libXau.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libXdmcp.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libdbus-1.so* /usr/lib/x86_64-linux-gnu/
#COPY --from=build /usr/lib/x86_64-linux-gnu/libbrotlicommon.so* /usr/lib/x86_64-linux-gnu/

# copy dummy sqlite3 package to prevent reinstallation of libsqlite3-0
COPY --from=build /tmp/libsqlite3-0_9.9.9_amd64.deb /tmp/libsqlite3-0_9.9.9_amd64.deb

# increase proxy timeout
# install chrome
RUN \
    echo "Add dummy package to prevent reinstallation of sqlite3-0 package with CVE" &&\
    dpkg -i /tmp/libsqlite3-0_9.9.9_amd64.deb &&\
    ldconfig \
 && sed -i 's|connectionTimeout=".*"|connectionTimeout="40000"|g' /opt/tomcat/conf/server.xml \
 && echo "Reinstall chrome to get all dependencies" &&\
    apt -y update && apt -y upgrade &&\
    apt-get install -y /tmp/google-chrome-stable_current_amd64.deb \
 && echo "removing packages containing critical CVEs" &&\
    apt remove -y libpython3.11-minimal \
 && apt clean

# set environment variables need to run METviewer
ENV CATALINA_HOME=/opt/tomcat
ENV PYTHONPATH="/METviewer-python/METcalcpy/:/METviewer-python/METplotpy/"
ENV METPLOTPY_BASE="/METviewer-python/METplotpy/"
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Set the PRE_LOAD_CHROME env variable to True to prevent invoking get_chrome_sync() from METplotpy
ENV PRE_LOAD_CHROME='True'

# Explicitly specify the shell to indicate that using the SHELL
# instruction is a conscious decision: 
# https://docs.docker.com/reference/build-checks/json-args-recommended/
SHELL ["/bin/sh", "-c"]
ENTRYPOINT ${CATALINA_HOME}/bin/startup.sh && /bin/bash
CMD ["true"]
