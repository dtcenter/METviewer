#!/bin/bash
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
# METviewerDir
# and METviewerTestCaseDir
# WILL BE ERASED AND REBUILT from the github source

# NOTE: This script is intended to run from cron.
# The gituser credentials should be stored in the git store - see https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage
# or with an ssh key or something. You won't be there to type the password...

# How to use this...
# 1) Make sure that you have a credentialed user for access to
# 	github.com/NCAR/METviewer.git and  https://github.com/NCAR/METviewer-test.git
#	and that you pass the user to -U and that you put the credentials into a git store for your user. Alternatively you could use ssh keys,
#	which might require a few modifications here.


usage() { echo "Usage: $0  -U <git user> -t<path to METviewer test directory> -s<db management system> -b<git branch>
-B<compare git branch> -l<path to met data> -d<mv_database> -m<path to METviewer home>
[-c(capture new images)] [-a address list] [-g<git tag>] [-G<compare git tag>] [-u<mv_user>]
[-p<mv_passwd>] [-h<mv_host>] [-P<mv_port>]
[-j<path to java executible>]" 1>&2; exit 1; }
export mv_test_db="mv_test"
export mv_user="mvuser"
export mv_pass="mvuser"
export mv_host="dakota.rap.ucar.edu"
export mv_port=3306
export git_user=""
export METviewerTag="HEAD"
export METviewerCompareTag="HEAD"
export capture=""
while getopts "U:t:b:B:l:d:m:a:g:G:u:p:h:P:s:j:c:T?" o; do
    case "${o}" in
    	c)
            capture="-c"
            ;;
        U)
            gituser=${OPTARG} # credentialed user for access to github.com/NCAR/METviewer.git and
            #  https://github.com/NCAR/METviewer-test.git
            ;;
        t)
            export METviewerTestDir=${OPTARG}
            ;;
        b)
            export METviewerBranch=${OPTARG}
            ;;
        B)
            export METviewerCompareBranch=${OPTARG}
            ;;
        s)
            optarglower=$(echo ${OPTARG} | /bin/tr '[:upper:]' '[:lower:]')
            if [ "${optarglower}" != "mysql" -a "${optarglower}" != "cb" -a "${optarglower}" != "mariadb" -a "${optarglower}" != "aurora" ]; then
                echo "db management system ${OPTARG} is not mysql or cb"
                usage
            fi
            export managementSystem=${optarglower}
            ;;
        l)
			if [ ! -d "${OPTARG}" ]; then
				echo "MET_DATA_DIR directory ${OPTARG} does not exist"
				usage
			fi
            export MET_DATA_DIR=${OPTARG}
            ;;
        d)
            mv_test_db=${OPTARG}
            ;;
        m)
            export METviewerDir=${OPTARG}
            ;;
        a)
            export addressList=${OPTARG}
            ;;
        g)
            METviewerTag="${OPTARG}"
            ;;
        G)
            METviewerCompareTag="${OPTARG}"
            ;;
        u)
            mv_user=${OPTARG}
            ;;
        p)
            mv_pass=${OPTARG}
            ;;
        h)
            mv_host=${OPTARG}
            ;;
        P)
            mv_port=${OPTARG}
            ;;
        j)
			if [ ! -x "${OPTARG}" ]; then
				echo "file ${OPTARG} does not exist or is not executable"
				usage
			fi
            export JAVA=${OPTARG}
			$JAVA -version
			if [ "$?" -ne "0" ]; then
			   echo "You provided a bad java executable";
			   usage;
			fi
            ;;
		?)
		usage
		;;
    esac
done
shift $((OPTIND-1))
# check for valid java
if [ -z ${JAVA+x} ]; then
	JAVA=$(which java)
fi
$JAVA -version
if [ "$?" -ne "0" ]; then
   echo "You do not have a java executable in your path";
   exit 1;
fi
# check for mandatory params
if [ -z ${gituser+x} ]; then
	echo "gituser is unset - exiting"
	usage
fi
if [ -z ${METviewerTestDir+x} ]; then
	echo "METviewerTestDir is unset - exiting"
	usage
fi
if [ -z ${METviewerBranch+x} ]; then
	echo "METviewerBranch is unset - exiting"
	usage
fi
if [ -z ${METviewerCompareBranch+x} ]; then
	echo "METviewerCompareBranch is unset - exiting"
	usage
fi
if [ -z ${MET_DATA_DIR+x} ]; then
	echo "MET_DATA_DIR is unset - exiting"
	usage
else
    # must be a valid directory
    if [ ! -d "${MET_DATA_DIR}" ]; then
				echo "directory ${MET_DATA_DIR} does not exist"
				usage
			fi
fi
if [ -z ${mv_test_db+x} ]; then
	echo "mv_test_db is unset - exiting"
	usage
fi
if [ -z ${METviewerDir+x} ]; then
	echo "METviewerDir is unset - exiting"
	usage
fi
# end of options processing

if [ ! -d "${METviewerTestDir}" ]; then
    mkdir -p ${METviewerTestDir}
fi

export METviewerBranchTestDir="${METviewerTestDir}/${METviewerBranch}/${METviewerTag}"
export METviewerCompareBranchTestDir="${METviewerTestDir}/${METviewerCompareBranch}/${METviewerCompareTag}"

METviewerTestDirTestCases="${METviewerTestDir}/test_cases_source"
logdir=~/MV_test_data/logs/nightly_${METviewerBranch}

# create log file and clone METviewer repo - checkout a local test branch
seconds=$(date +%s)
mkdir -p $logdir
logfile=${logdir}/${seconds}
touch $logfile
rm -rf ${METviewerDir}
rm -rf ${METviewerTestDirTestCases}
echo "clone the METviewer repo"
git clone https://${gituser}@github.com/NCAR/METviewer.git ${METviewerDir}
#echo git fetch --all
#git fetch --all
# checkout code to proper branch and use -test for local copy
cdir=$(pwd)
cd ${METviewerDir}
if [ ${METviewerTag} = "HEAD" ]; then
    # check to see if branch exists
    echo "git rev-parse --verify --quiet \"remotes/origin/${METviewerBranch}\""
    git rev-parse --verify --quiet "remotes/origin/${METviewerBranch}"
    if [ $? -ne 0 ]; then
        echo "branch \"remotes/origin/${METviewerBranch}\" does not exist in METviewer repository"
        usage
    fi
    git checkout remotes/origin/${METviewerBranch} -b "${METviewerBranch}-test"
else
    # check to see if tag exists
    echo git rev-parse --verify --quiet ${METviewerTag}
    git rev-parse --verify --quiet ${METviewerTag}
    if [ $? -ne 0 ]; then
        echo "tag ${METviewerTag} does not exist in METviewer repository"
        usage
    fi
    git checkout tags/${METviewerTag} -b "${METviewerBranch}-test"
fi

cd $cdir
mkdir -p ${METviewerTestDir}

echo "clone the test cases repo"
if [ ! -d "${METviewerBranchTestDir}" ]; then
    mkdir -p ${METviewerBranchTestDir}
else
    rm -rf ${METviewerBranchTestDir}
fi
git clone https://${gituser}@github.com/NCAR/METviewer-test.git ${METviewerBranchTestDir}/test_data
# the test cases are on the unversioned master branch for now. checkout a local called test - just to be safe
cd ${METviewerBranchTestDir}/test_data
git checkout remotes/origin/master -b test
cd $cdir

# Fix up the test directory
#NOTE: The met load data 'met_data' is stored locally for now in $MET_DATA_DIR. Eventually this should also come from some common data repository
# and the mv_mysql.sql is delivered in the METviewer code
# and the mv_load.xml is checked into the test cases and must be moved here for the load test. It is needed for the load test but causes failures in mv_test where it is currently checked in
# This should probably all be straightened out a little but for now we fix it up here.
ln -sf ${MET_DATA_DIR}/met_data ${METviewerBranchTestDir}
mkdir -p ${METviewerBranchTestDir}/load_data/load/
cp ${METviewerDir}/sql/mv_mysql.sql  ${METviewerBranchTestDir}/load_data/load
cp ${METviewerBranchTestDir}/test_data/test_cases/loading/load_test.xml ${METviewerBranchTestDir}/load_data/load
# replace host database user and password in load_test.xml
sed -i .bak "s/<host>.*<\/host>/<host>${mv_host}:${mv_port}<\/host>/" ${METviewerBranchTestDir}/load_data/load/load_test.xml
sed -i .bak "s/<database>.*<\/database>/<database>${mv_test_db}<\/database>/" ${METviewerBranchTestDir}/load_data/load/load_test.xml
sed -i .bak "s/<user>.*<\/user>/<user>${mv_user}<\/user>/" ${METviewerBranchTestDir}/load_data/load/load_test.xml
sed -i .bak "s/<password>.*<\/password>/<password>${mv_pass}<\/password>/" ${METviewerBranchTestDir}/load_data/load/load_test.xml
sed -i .bak "s#path_to_data_dir#${METviewerBranchTestDir}/met_data/#" ${METviewerBranchTestDir}/load_data/load/load_test.xml
rm -rf ${METviewerBranchTestDir}/test_data/test_cases/loading  # have to do this because it interferes with the test runner
ln -sf ${METviewerDir}/R_tmpl ${METviewerBranchTestDir}/R_tmpl
mkdir ${METviewerBranchTestDir}/R_work
mkdir ${METviewerBranchTestDir}/output
ln -sf ${METviewerDir}/R_work/include ${METviewerBranchTestDir}/R_work/include
mkdir ${METviewerBranchTestDir}/output/data
mkdir ${METviewerBranchTestDir}/output/plots
mkdir ${METviewerBranchTestDir}/output/scripts

# clean and build the code
cd ${METviewerDir}
ant clean
ret=$?
if [ $ret -ne 0 ]; then
	#send a note
	if [ "X$addressList" != "X" ]; then
		cat $logfile | mail -s "nightly_${METviewerBranch} ant clean failed with ret code $ret - here is the log file" $addressList
	else
		echo "nightly_${METviewerBranch} ant clean failed with return code $ret logfile $(hostname):$logfile"
		cat $logFile
	fi
	exit $ret
fi
ant all -Ddb.management.system=${managementSystem}
ret=$?
if [ $ret -ne 0 ]; then
	#send a note
	if [ "X$addressList" != "X" ]; then
		cat $logfile | mail -s "nightly_${METviewerBranch} ant all failed with ret code $ret - here is the log file" $addressList
	else
		echo "nightly_${METviewerBranch} ant all failed with return code $ret logfile $(hostname):$logfile"
		cat $logFile
	fi
	exit $ret
fi

# run the mv_test
#send a note
if [ "X$addressList" != "X" ]; then
	echo "running /bin/sh ./bin/mv_test.sh -t ${METviewerBranchTestDir} -m ${METviewerDir} -d ${mv_test_db} -u ${mv_user} -p ${mv_pass} -h ${mv_host} -P ${mv_port} -l ${capture} -n> ${logfile}"
	/bin/sh ./bin/mv_test.sh -m${METviewerDir} -t${METviewerBranchTestDir} -d${mv_test_db} -u${mv_user} -p${mv_pass} -h${mv_host} -P${mv_port} -k${managementSystem}  -l ${capture} -n > ${logfile}

	ret=$?
	echo mv_test ret is $ret
	cat $logfile | mail -s "nightly_${METviewerBranch} mv_test failed with $ret failures - here is the log file" $addressList
else
	echo "running /bin/sh ./bin/mv_test.sh -t${METviewerBranchTestDir} -m${METviewerDir} -d${mv_test_db} -u${mv_user} -p${mv_pass} -h${mv_host} -P${mv_port} -l ${capture} -n"
    /bin/sh  ./bin/mv_test.sh -m${METviewerDir} -t${METviewerBranchTestDir} -d${mv_test_db} -u${mv_user} -p${mv_pass} -h${mv_host} -P${mv_port} -k${managementSystem} -l ${capture} -n
	ret=$?
	echo mv_test ret is $ret
fi

# run the mv_compare
if [ "X$addressList" != "X" ]; then
	echo "running /bin/sh ./bin/mv_compare.sh  -m ${METviewerDir}  -t ${METviewerBranchTestDir} -c ${METviewerCompareBranchTestDir} > ${logfile}"
	/bin/sh  ./bin/mv_compare.sh  -m ${METviewerDir} -t ${METviewerBranchTestDir} -c
	${METviewerCompareBranchTestDir} > ${logfile}
	ret=$?
	echo mv_test ret is $ret
	cat $logfile | mail -s "nightly_${METviewerBranch} mv_compare failed with $ret failures - here is the log file" $addressList
else
	echo "running /bin/sh ./bin/mv_compare.sh  -m ${METviewerDir} -t ${METviewerBranchTestDir} -c ${METviewerCompareBranchTestDir}"
    /bin/sh  ./bin/mv_compare.sh  -m ${METviewerDir} -t ${METviewerBranchTestDir} -c ${METviewerCompareBranchTestDir}
	ret=$?
	echo mv_test ret is $ret
fi

exit $ret
