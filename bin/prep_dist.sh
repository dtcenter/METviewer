
MV_SRC=src/metviewer
MV_DEST=.

#  checkout a clean copy of the source
echo "Getting source from CVS..."
if [[ -d $MV_SRC ]]; then
	rm -rf $MV_SRC
fi
mkdir $MV_SRC
echo "cvs checkout -d $MV_SRC apps/verif/metviewer"
cvs checkout -d $MV_SRC apps/verif/metviewer

#  make a copy of the source tree
echo "Copying source tree"
cp -R $MV_SRC $MV_DEST

echo "Scrubbing source dist bundle"

#  remove all CVS folders
#find $MV_DEST -name 'CVS' -exec rm -rf {} \;
rm -rf $(find $MV_DEST/metviewer -name 'CVS')

#  scrub system info from the log4j.properties file 
more $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties | \
    sed -r 's/File=.*/File=/' > $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties_new
rm $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties
mv $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties_new \
   $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/log4j.properties

#  scrub system info from the mvservlet.properties file
more $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties | \
    sed -r 's/=.*/=/' | sed -r 's/^[^=]+$//' > $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties_new
rm $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties
mv $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties_new \
   $MV_DEST/metviewer/webapp/metviewer/WEB-INF/classes/mvservlet.properties

#  scrub system info from the mv_load.sh file 
more $MV_DEST/metviewer/bin/mv_load.sh | sed -r 's/JAVA=.*/JAVA=$(which java)/' | sed -r 's/MV_HOME=.*/MV_HOME=/' \
    > $MV_DEST/metviewer/bin/mv_load.sh_new
rm $MV_DEST/metviewer/bin/mv_load.sh
mv $MV_DEST/metviewer/bin/mv_load.sh_new $MV_DEST/metviewer/bin/mv_load.sh
chmod +x $MV_DEST/metviewer/bin/mv_load.sh

#  scrub system info from the mv_batch.sh file 
more $MV_DEST/metviewer/bin/mv_batch.sh | sed -r 's/JAVA=.*/JAVA=$(which java)/' | sed -r 's/MV_HOME=.*/MV_HOME=/' \
    > $MV_DEST/metviewer/bin/mv_batch.sh_new
rm $MV_DEST/metviewer/bin/mv_batch.sh
mv $MV_DEST/metviewer/bin/mv_batch.sh_new $MV_DEST/metviewer/bin/mv_batch.sh
chmod +x $MV_DEST/metviewer/bin/mv_batch.sh

echo "Done"

