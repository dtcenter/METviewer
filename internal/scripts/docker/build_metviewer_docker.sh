#! /bin/bash

echo "Running script to build METviewer and its dependencies in Docker"

# GitHub constants
export METPLOTPY_GIT_URL=https://github.com/dtcenter/METplotpy
export METCALCPY_GIT_URL=https://github.com/dtcenter/METcalcpy
export METDATAIO_GIT_URL=https://github.com/dtcenter/METdataio

# Software constants
export TOMCAT_MAJOR_VERSION=9
export TOMCAT_MINOR_VERSION=0.89
export TOMCAT_VERSION=${TOMCAT_MAJOR_VERSION}.${TOMCAT_MINOR_VERSION}

# Check for required environment variables
if [ -z ${CATALINA_HOME+x} ]; then
  echo "ERROR: \${CATALINA_HOME} is required!"
  exit 1
fi
if [ -z ${METPLOTPY_GIT_NAME+x} ]; then
  echo "ERROR: \${METPLOTPY_GIT_NAME} is required!"
  exit 1
fi
if [ -z ${METCALCPY_GIT_NAME+x} ]; then
  echo "ERROR: \${METCALCPY_GIT_NAME} is required!"
  exit 1
fi
if [ -z ${METDATAIO_GIT_NAME+x} ]; then
  echo "ERROR: \${METDATAIO_GIT_NAME} is required!"
  exit 1
fi

# Update the OS, as needed
apt update
apt -y upgrade

# Install required packages
apt -y install ant openjdk-17-jdk r-base mariadb-server ksh

# Setup default cran repo
echo "r <- getOption('repos'); r['CRAN'] <- 'http://cran.us.r-project.org'; options(repos = r);" > ~/.Rprofile

# Install required R packages
Rscript -e "install.packages('boot')"
Rscript -e "install.packages('plotrix')"
Rscript -e "install.packages('gsl')"
Rscript -e "install.packages('data.table')"
Rscript -e "install.packages('verification')"

# Install Tomcat
wget https://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR_VERSION}/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz
tar -xvf apache-tomcat-${TOMCAT_VERSION}.tar.gz
rm apache-tomcat*.tar.gz
mv apache-tomcat* ${CATALINA_HOME}
chmod +x ${CATALINA_HOME}/bin/*sh

# Install METplus python components
mkdir /METviewer-python
cd /METviewer-python

echo "Checking out METcalcpy ${METCALCPY_GIT_NAME} from ${METCALCPY_GIT_URL}"
git clone --branch ${METCALCPY_GIT_NAME} ${METCALCPY_GIT_URL}

echo "Checking out METplotpy ${METPLOTPY_GIT_NAME} from ${METPLOTPY_GIT_URL}"
git clone --branch ${METPLOTPY_GIT_NAME} ${METPLOTPY_GIT_URL}

echo "Checking out METdataio ${METDATAIO_GIT_NAME} from ${METDATAIO_GIT_URL}"
git clone --branch ${METDATAIO_GIT_NAME} ${METDATAIO_GIT_URL}

# Install METviewer
echo "Configuring and building METviewer"
cd /METviewer
cat webapp/metviewer/WEB-INF/classes/build.properties | \
    sed -r 's%db.host=.*%db.host=mysql_mv%g' | \
    sed -r 's%db.user=.*%db.user=root%g' | \
    sed -r 's%db.password=.*%db.password=mvuser%g' | \
    sed -r 's%db.management.system=.*%db.management.system=mysql%g' | \
    sed -r 's%output.dir=.*%output.dir=/opt/tomcat/webapps/metviewer_output/%g' | \
    sed -r 's%webapps.dir=.*%webapps.dir=/opt/tomcat/webapps/metviewer/%g' | \
    sed -r 's%url.output=.*%url.output=http://localhost:8080/metviewer_output/%g' | \
    sed -r 's%python.env=.*%python.env=/usr/%g' | \
    sed -r 's%metcalcpy.home=.*%metcalcpy.home=/METviewer-python/METcalcpy/%g' | \
    sed -r 's%metplotpy.home=.*%metplotpy.home=/METviewer-python/METplotpy/%g' \
    > build.properties

ant -Dbuild.properties.file=./build.properties \
    -Ddb.management.system=mysql \
    -Dmetcalcpy.path=/METviewer-python/METcalcpy/ \
    -Dmetplotpy.path=/METviewer-python/METplotpy/ \
    -Dpython.env.path=/usr/ war \

mv /METviewer/dist/*.war ${CATALINA_HOME}/webapps

echo "Configuring METviewer scripts"
cd /METviewer/bin

cat mv_batch.sh | \
    sed -r 's%JAVA=.*%JAVA=java\nMV_HOME=/METviewer%g' | \
    sed -r 's%PYTHON_ENV=.*%PYTHON_ENV=/usr%g' | \
    sed -r 's%METCALCPY_HOME=.*%METCALCPY_HOME=/METviewer-python/METcalcpy/%g' | \
    sed -r 's%METPLOTPY_HOME=.*%METPLOTPY_HOME=/METviewer-python/METplotpy/%g' \
    >  mv_batch.sh-DOCKER
mv mv_batch.sh-DOCKER mv_batch.sh

cat mv_load.sh | \
    sed -r 's%PYTHON_ENV=.*%PYTHON_ENV=/usr%g'  | \
    sed -r 's%METDATAIO_HOME=.*%METDATAIO_HOME=/METviewer-python/METdataio/%g' \
    > mv_load.sh-DOCKER
mv mv_load.sh-DOCKER mv_load.sh \

cat mv_scorecard.sh | \
    sed -r 's%JAVA=.*%JAVA=java\nMV_HOME=/METviewer%g' | \
    sed -r 's%PYTHON_ENV=.*%PYTHON_ENV=/usr%g' | \
    sed -r 's%METCALCPY_HOME=.*%METCALCPY_HOME=/METviewer-python/METcalcpy/%g' | \
    sed -r 's%METPLOTPY_HOME=.*%METPLOTPY_HOME=/METviewer-python/METplotpy/%g' \
    >  mv_scorecard.sh-DOCKER
mv mv_scorecard.sh-DOCKER mv_scorecard.sh

cat mv_prune.sh | \
    sed -r 's%JAVA=.*%JAVA=java\nMV_HOME=/METviewer%g' | \
    sed -r 's%PYTHON_ENV=.*%PYTHON_ENV=/usr%g' | \
    sed -r 's%METCALCPY_HOME=.*%METCALCPY_HOME=/METviewer-python/METcalcpy/%g' | \
    sed -r 's%METPLOTPY_HOME=.*%METPLOTPY_HOME=/METviewer-python/METplotpy/%g' \
    >  mv_prune.sh-DOCKER
mv mv_prune.sh-DOCKER mv_prune.sh

# Create links for python3
ln -sf /usr/local/bin/python3.10 /usr/bin/python3
ln -sf /usr/bin/python3 /usr/bin/python

ln -sf /usr/local/bin/pip3.10 /usr/bin/pip3
ln -sf /usr/bin/pip3 /usr/bin/pip

# Install GEOS - needed for cartopy
cd /tmp
wget http://download.osgeo.org/geos/geos-3.7.2.tar.bz2
tar xjf geos-3.7.2.tar.bz2
cd /tmp/geos-3.7.2
./configure --enable-php; make clean ; make
make install
ldconfig
cd /tmp
rm -r geos-3.7.2.tar.bz2

# Install Python packages
pip install cartopy && \
pip install eofs && \
pip install imutils==0.5.4 && \
pip install imageio==2.19.2 && \
pip install lxml==4.9.1 && \
pip install matplotlib==3.5.2 && \
pip install netcdf4==1.6.2 && \
pip install numpy==1.22.0 && \
pip install pytest==7.1.2 && \
pip install metpy==1.3.1 && \
pip install pyyaml==6.0 && \
pip install scikit-image==0.19.3 && \
pip install scikit-learn && \
pip install scipy==1.11.1 && \
pip install xarray==2022.3.0 && \
pip install PyMySQL==1.0.2 && \
pip install pint==0.19.2 && \
pip install plotly==5.9.0 && \
pip install kaleido==0.2.1 && \
pip install attrs==22.1.0 && \
pip install exceptiongroup==1.0.4 && \
pip install iniconfig==1.1.1 && \
pip install packaging==22.0 && \
pip install pluggy==1.0.0 && \
pip install pytz==2022.6 && \
pip install setuptools==65.5.1 && \
pip install six==1.16.0 && \
pip install tomli==2.0.1 && \
pip install wheel==0.38.1 && \
pip install python-dateutil==2.8.2 && \
pip install opencv-python && \
pip install pandas==1.5.2

# Remove unneeded scripts
rm /METviewer/bin/auto_test.sh \
   /METviewer/bin/mv_test.sh \
   /METviewer/bin/nightly_test.sh \
   /METviewer/bin/prep_dist.sh \
   /METviewer/bin/mv_compare.sh
rm -r /METviewer/test

# Change permissions of the scripts
chmod 755 /METviewer/bin/*.sh
