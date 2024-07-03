#! /bin/bash

echo "Running script to build METviewer and its dependencies in Docker"

# GitHub constants
export METPLOTPY_GIT_URL=https://github.com/dtcenter/METplotpy
export METCALCPY_GIT_URL=https://github.com/dtcenter/METcalcpy
export METDATAIO_GIT_URL=https://github.com/dtcenter/METdataio

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
apt update && apt -y upgrade

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

# Remove unneeded scripts
rm /METviewer/bin/auto_test.sh \
   /METviewer/bin/mv_test.sh \
   /METviewer/bin/nightly_test.sh \
   /METviewer/bin/prep_dist.sh \
   /METviewer/bin/mv_compare.sh
rm -r /METviewer/test

# Change permissions of the scripts
chmod 755 /METviewer/bin/*.sh
