#!/bin/bash

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
#
# This utility removes all output data older that 7 days that METViewer web app produces
#
# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

find /var/www/dtcenter/met/metviewer_output/plots -mtime +7 -exec rm {} \;
find /var/www/dtcenter/met/metviewer_output/xml -mtime +7 -exec rm {} \;
find /var/www/dtcenter/met/metviewer_output/data -mtime +7 -exec rm {} \;
find /var/www/dtcenter/met/metviewer_output/scripts -mtime +7 -exec rm {} \;

#Run backup script at 0 AM  everyday
# 0 0 * * * /d3/projects/METViewer/src_dev/apps/verif/metviewer/bin/webapp_cleanup.sh