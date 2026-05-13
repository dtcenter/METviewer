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
echo " run this file where the build.xml file resides"
echo " The appropriate versions of METdataio, METcalcpy, METplotpy, and METviewer are being used"

echo" appropriate changes made to the metviewer1.jsp file with updated version number"
echo "=======================================================================================" 


# Build the war file using the ant on /d1/mwin/apache-ant-1.10.14/bin/ant
echo "======================================================================="
echo "Building the war file..."
echo "======================================================================="
echo ""
/d1/mwin/apache-ant-1.10.14/bin/ant \
-Dbuild.properties.file=/d3/projects/METViewer/src/build.properties \
-Ddb.management.system=mariadb \
-Dmetcalcpy.path=/d3/projects/METViewer/METcalcpy/ \
-Dmetplotpy.path=/d3/projects/METViewer/METplotpy/ \
-Dmetdataio.path=/d3/projects/METViewer/METdataio/ \
-Dpython.env.path=/d3/projects/METViewer/METviewer_py3.14/ clean all

# copy the war file as metviewer_dev.war to the tomcat directory
echo "================================================================================="
echo "Copying the war file to /opt/tomcat10/vxwww/webapps/metviewer_dev as metviewer_dev.war"
echo "================================================================================="
echo ""

cp /d3/projects/METViewer/src/apps/METviewer/dist/metviewer.war /opt/tomcat10/vxwww/webapps/metviewer_dev.war

echo "======================================================================="
echo "Now restarting catalina... !!!YOU NEED TO PROVIDE THE PASSWORD!!!"
echo "======================================================================="
echo ""
/opt/tomcat10/vxwww/bin/startup.sh
