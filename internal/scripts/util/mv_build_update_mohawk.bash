#!/usr/bin/sh


# This script builds the METviewer war file, then copies it to the tomcat directory
# This script is set up for dakota
echo "==================================================================================================="
echo " This script builds the METviewer war file, copies it to the tomcat directory as metviewer_dev.war" 
echo " As a last step, the tomcat server is restarted, but the user must supply a password when prompted."
echo "==================================================================================================="
echo ""
echo ""
echo ""



echo "=======================================================================================" 

echo "!!!PRE-CONDITIONS!!!:"

echo " su as the appropriate user, vxww"
echo " The appropriate versions of METdataio, METcalcpy, METplotpy, and METviewer are being used"

echo" appropriate changes made to the metviewer1.jsp file with updated version number"
echo "=======================================================================================" 


# Build the war file using the ant on /d2/personal/mwin/apache-ant-1.10.14/bin/ant
echo "======================================================================="
echo "Building the war file..."
echo "======================================================================="
echo ""
/home/minnawin/apache-ant-1.10.14/bin/ant \
-Dbuild.properties.file=/d2/projects/METViewer/src/build.properties \
-Ddb.management.system=mariadb \
-Dmetcalcpy.path=/d2/projects/METViewer/METcalcpy/ \
-Dmetplotpy.path=/d2/projects/METViewer/METplotpy/ \
-Dmetdataio.path=/d2/projects/METViewer/METdataio/ \
-Dpython.env.path=/d2/projects/METViewer/METviewer_py3.12/ clean all

# copy the war file as metviewer_dev.war to the tomcat directory
echo "================================================================================="
echo "Copying the war file to /opt/tomcat/vxwww/webapps/metviewer_dev as metviewer_dev.war"
echo "================================================================================="
echo ""

cp /d2/projects/METViewer/src/apps/METviewer/dist/metviewer.war /opt/tomcat/vxwww/webapps/metviewer.war

echo "======================================================================="
echo "Now restarting catalina"
echo "======================================================================="
echo ""
/opt/tomcat/vxwww/bin/startup.sh

