#!/bin/env bash
# NOTE:
# Currently the met test load data files are not controlled so this script assumes that they are local.
# This script assumes a directory structure like this...
#met_data/ens_vsdb
#met_data/noahmp
#met_data/precip_vsdb
#met_data/meso_vsdb
#met_data/grid_stat
#met_data/afwaoc
#met_data/CVS
#met_data/point_stat
#met_data/ensemble_stat
# your met_data directory might be named something else so you have to set it in the "modify these" variables section.

# The two directories named in 
# METViewerDir=~/METViewer
# and METViewerTestDir=~\METViewerTest
# WILL BE ERASED AND REBUILT from the github source

# NOTE: This script is intended to run from cron.
# The gituser credentials should be stored in the git store - see https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage
# or with an ssh key or something. You won't be there to type the password...

# How to use this...
# 1) Make sure that you have a credentialed user for access to 
# 	github.com/dtcenter/METViewer.git and  https://github.com/dtcenter/METViewer-test.git
#	and that you put the credentials into a git store for your user. Alternatively you could use ssh keys, 
#	which might require a few modifications here.
# 2) modify the "modify these" variables below as necessary - remember these directories WILL BE OVERWRITTEN.
# 3) modify the "mv_test" parameters per the usage in mv_test.sh
#

# modify these variables for your system....
gituser='somegituser' # credentials stored in git store (or change to use ssh or something) - see https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage
addressList="" # initially empty - set by script parameter (comma seperated email addresses) to notify with results
if [ $# -ne 0 ]; then
	addressList=$1
fi
addressList=$1 # who to notify with results
METViewerDir=~/METViewer # your directory to hold metviewer code
METViewerBranch=mv_2_5_dev  # branch under test
METViewerTestDir=~/METViewerTest # directory to hold test code
MET_DATA_DIR=/home/someuser/METViewerTestData/met_data # directory that holds the met data for the load test
mv_test_db=mv_test  # metviewer test directory
mv_user=met_admin_user  # metviewer database test user - needs authority to read databases from schema...
mv_pass="met_admin_user_password" # metviewer database test user password
mv_host=mvtest.something.something.gov # metviewer test database host
mv_port=3306  # metviewer test database port - mysql usually 3306
logdir=~/MV_test_data/logs/nightly_${METViewerBranch}
# end modify these

# create log file and clone METViewer repo - checkout a local test branch
seconds=$(date +%s)
mkdir -p $logdir
logfile=${logdir}/${seconds}
touch $logfile
rm -rf ${METViewerDir}
rm -rf ${METViewerTestDir}
git clone https://${gituser}@github.com/dtcenter/METViewer.git ${METViewerDir}
# checkout code to proper branch and use -test for local copy
cd ${METViewerDir}
git checkout remotes/origin/${METViewerBranch} -b "${METViewerBranch}-test"
git branch -a
cd ..
mkdir -p ${METViewerTestDir}

# clone in the test cases repo
git clone https://${gituser}@github.com/dtcenter/METViewer-test.git ${METViewerTestDir}/test_data
# the test cases are on the unversioned master branch for now. checkout a local called test - just to be safe
cd ${METViewerTestDir}/test_data
git checkout remotes/origin/master -b test
cd ../..

# Fix up the test directory
#NOTE: The met load data 'met_data' is stored locally for now in $MET_DATA_DIR. Eventually this should also come from some common data repository
# and the mv_mysql.sql is delivered in the METViewer code
# and the mv_load.xml is checked into the test cases and must be moved here for the load test. It is needed for the load test but causes failures in mv_test where it is currently checked in
# This should probably all be straightened out a little but for now we fix it up here.
ln -sf ${MET_DATA_DIR} ${METViewerTestDir}/met_data
mkdir -p ${METViewerTestDir}/load_data/load/
cp ${METViewerDir}/sql/mv_mysql.sql  ${METViewerTestDir}/load_data/load
ln -sf ${METViewerTestDir}/test_data/test_cases ${METViewerTestDir}/plots_batch
mv  ${METViewerTestDir}/test_data/test_cases/loading/load_test.xml ${METViewerTestDir}/load_data/load
ln -sf ${METViewerDir}/R_tmpl ${METViewerTestDir}/R_tmpl
mkdir ${METViewerTestDir}/R_work
ln -sf ${METViewerDir}/R_work/include ${METViewerTestDir}/R_work/include
mkdir ${METViewerTestDir}/R_work/data
mkdir ${METViewerTestDir}/R_work/plots
mkdir ${METViewerTestDir}/R_work/scripts

# clean and build the code
cd ${METViewerDir}
ant clean
ret=$?
if [ $ret -ne 0 ]; then
	#send a note
	if [ "X$addressList" != "X" ]; then
		cat $logfile | mail -s "nightly_${METViewerBranch} ant clean failed with ret code $ret - here is the log file" $addressList
	else 
		echo "nightly_${METViewerBranch} ant clean failed with return code $ret logfile $(hostname):$logfile"
		cat $logFile
	fi
	exit $ret
fi
ant all
ret=$?
if [ $ret -ne 0 ]; then
	#send a note
	if [ "X$addressList" != "X" ]; then
		cat $logfile | mail -s "nightly_${METViewerBranch} ant all failed with ret code $ret - here is the log file" $addressList
	else 
		echo "nightly_${METViewerBranch} ant all failed with return code $ret logfile $(hostname):$logfile"
		cat $logFile
	fi
	exit $ret
fi

# run the mv_test - the all parameter also loads the test data
#send a note
if [ "X$addressList" != "X" ]; then
	echo "running /bin/sh ${METViewerDir}/bin/mv_test.sh -m ${METViewerDir} -t ${METViewerTestDir} -d ${mv_test_db} -u ${mv_user} -p ${mv_pass} -h ${mv_host} -P ${mv_port}  ${METViewerTestDir} all"
	/bin/sh ${METViewerDir}/bin/mv_test.sh -m ${METViewerDir} -t ${METViewerTestDir} -d ${mv_test_db} -u ${mv_user} -p ${mv_pass} -h ${mv_host} -P ${mv_port}  ${METViewerTestDir} all > ${logfile} 
	ret=$?
	echo mv_test ret is $ret
	cat $logfile | mail -s "nightly_${METViewerBranch} mv_test failed with $ret failures - here is the log file" $addressList
else 
	echo "running /bin/sh ${METViewerDir}/bin/mv_test.sh -m ${METViewerDir} -t ${METViewerTestDir} -d ${mv_test_db} -u ${mv_user} -p ${mv_pass} -h ${mv_host} -P ${mv_port}  ${METViewerTestDir} all"
	/bin/sh ${METViewerDir}/bin/mv_test.sh -m ${METViewerDir} -t ${METViewerTestDir} -d ${mv_test_db} -u ${mv_user} -p ${mv_pass} -h ${mv_host} -P ${mv_port}  ${METViewerTestDir} all
	ret=$?
	echo mv_test ret is $ret
fi
exit $ret

	
