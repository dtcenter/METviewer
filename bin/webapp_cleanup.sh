#!/bin/bash

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#
# This utility removes all output data older that 2 days that METViewer web app produces
#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

find /opt/tomcat/webapps/metviewer/plots -mtime +2 -exec rm {} \;
find /opt/tomcat/webapps/metviewer/xml -mtime +2 -exec rm {} \;
find /opt/tomcat/webapps/metviewer/R_work/data -mtime +2 -exec rm {} \;
find /opt/tomcat/webapps/metviewer/R_work/scripts -mtime +2 -exec rm {} \;

#Run backup script at 0 AM  everyday
# 0 0 * * * /d3/projects/METViewer/src_dev/apps/verif/metviewer/bin/webapp_cleanup.sh