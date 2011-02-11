
MV_SRC=/home/pgoldenb/ncep_mv/src/apps/verif/metviewer
MV_DEST=/home/pgoldenb/ncep_mv

#  make a copy of the source tree
cp -R $MV_SRC $MV_DEST

#  remove all CVS folders
#find $MV_DEST -name 'CVS' -exec rm -rf {} \;
rm -rf $(find $MV_DEST -name 'CVS')

#  scrub system info from the log4j.properties file 
more $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties | \
    sed -r 's/File=.*/File=/' > $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties_new
rm $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties
mv $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties_new \
   $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties

#  scrub system info from the mvservlet.properties file
more $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties | \
    sed -r 's/=.*/=/' > $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties_new
rm $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties
mv $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties_new \
   $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties



