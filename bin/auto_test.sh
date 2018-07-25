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
# METViewerDir
# and METViewerTestCaseDir
# WILL BE ERASED AND REBUILT from the github source

# NOTE: This script is intended to run from cron.
# The gituser credentials should be stored in the git store - see https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage
# or with an ssh key or something. You won't be there to type the password...

# How to use this...
# 1) Make sure that you have a credentialed user for access to 
# 	github.com/NCAR/METViewer.git and  https://github.com/NCAR/METViewer-test.git
#	and that you pass the user to -U and that you put the credentials into a git store for your user. Alternatively you could use ssh keys,
#	which might require a few modifications here.


usage() { echo "Usage: $0  -U <git user> -b<git branch>|-n -t<path to METViewer test directory>  -B<compare git branch> -l<path to met data> -d<mv_database> -m<path to METViewer home> [-a address list] [-g<git tag>] [-G<compare git tag>] [-u<mv_user>] [-p<mv_passwd>] [-h<mv_host>] [-P<mv_port>] [-j<path to java executible>]" 1>&2; exit 1; }
export mv_test_db="mv_test"
export mv_user="mvuser"
export mv_pass="mvuser"
export mv_host="dakota.rap.ucar.edu"
export mv_port=3306
export METViewerTag="HEAD"
export METViewerCompareTag="HEAD"
unset gituser
unset METViewerTestDir
unset METViewerCompareBranch
unset METViewerDir
unset MET_DATA_DIR
unset JAVA

while getopts "U:t:b:B:l:d:m:a:g:G:u:p:h:P:j:n?" o; do
    case "${o}" in
        n) export noClone=true # do not rm and clone the METViewer directory - it is probably under construction
            ;;
        U)
            export gituser=${OPTARG} # credentialed user for access to github.com/NCAR/METViewer.git and  https://github.com/NCAR/METViewer-test.git
            ;;
        t)
            export METViewerTestDir=${OPTARG}
            ;;
        b)
            export METViewerBranch=${OPTARG}
            ;;
        B)
            export METViewerCompareBranch=${OPTARG}
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
            export METViewerDir=${OPTARG}
            ;;
        a)
            export addressList=${OPTARG}
            ;;
        g)
            METViewerTag="${OPTARG}"
            ;;
        G)
            METViewerCompareTag="${OPTARG}"
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
				echo "file ${OPTARG} does not exist or is not executible"
				usage
			fi
            export JAVA=${OPTARG}
			$JAVA -version
			if [ "$?" -ne "0" ]; then
			   echo "You provided a bad java executible";
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
   echo "You do not have a java executible in your path";
   exit 1;
fi
# check for mandatory params
if [ -z "${gituser+x}" ]; then
	echo "gituser is unset - exiting"
	usage
fi
if [ -z "${METViewerTestDir+x}" ]; then
	echo "METViewerTestDir is unset - exiting"
	usage
fi
if [ -z "${METViewerCompareBranch+x}" ]; then
	echo "METViewerCompareBranch is unset - exiting"
	usage
fi
if [ -z "${METViewerDir+x}" -a -z "${METViewerBranch+x}" -a -z "${gituser+x}" ]; then
	echo "METViewerBranch is unset and gituser is unset and local (-m) METViewer dir is unset - exiting"
	usage
else
    if [ ! -z "${METViewerDir+x}" ]; then # MET_VIEWER_DIR is set
        # MET_VIEWER_DIR is set - set the branch and tag
        if [ ! -d "${METViewerDir}" ]; then
				echo "MET_VIEWER_DIR directory ${METViewerDir} does not exist"
				usage
			fi
        pushd ${METViewerDir}
        git branch
        if [[ $? -ne 0 ]]; then
            METViewerBranch="unknown"
        else
            METViewerBranch=$(git branch | grep "*" | awk '{print $2}')
        fi
        popd
    fi
fi

if [ -z "${MET_DATA_DIR+x}" ]; then
	echo "MET_DATA_DIR is unset - exiting"
	usage
else
    # must be a valid directory
    if [ ! -d "${MET_DATA_DIR}" ]; then
				echo "directory ${MET_DATA_DIR} does not exist"
				usage
			fi
fi
if [ -z "${mv_test_db+x}" ]; then
	echo "mv_test_db is unset - exiting"
	usage
fi
if [ -z "${METViewerDir+x}" ]; then
	echo "METViewerDir is unset - exiting"
	usage
fi
# end of options processing

if [ ! -d "${METViewerTestDir}" ]; then
    mkdir -p ${METViewerTestDir}
fi

export METViewerBranchTestDir="${METViewerTestDir}/${METViewerBranch}/${METViewerTag}"
export METViewerCompareBranchTestDir="${METViewerTestDir}/${METViewerCompareBranch}/${METViewerCompareTag}"

METViewerTestDirTestCases="${METViewerTestDir}/test_cases_source"
logdir=~/MV_test_data/logs/nightly_${METViewerBranch}

# create log file and clone METViewer repo - checkout a local test branch
seconds=$(date +%s)
mkdir -p $logdir
logfile=${logdir}/${seconds}
touch $logfile

cdir=$(pwd)
if [ -z "${noClone+x}" ]; then   # if not noClone then clone the METViewer directory
    # gituser is set so clone the repo
    rm -rf ${METViewerDir}
    rm -rf ${METViewerTestDirTestCases}
    echo "clone the METViewer repo"
    git clone https://${gituser}@github.com/NCAR/METViewer.git ${METViewerDir}
    #echo git fetch --all
    #git fetch --all
    # checkout code to proper branch and use -test for local copy
    cd ${METViewerDir}
    if [ ${METViewerTag} = "HEAD" ]; then
        # check to see if branch exists
        echo "git rev-parse --verify --quiet \"remotes/origin/${METViewerBranch}\""
        git rev-parse --verify --quiet "remotes/origin/${METViewerBranch}"
        if [ $? -ne 0 ]; then
            echo "branch \"remotes/origin/${METViewerBranch}\" does not exist in METViewer repository"
            usage
        fi
        git checkout remotes/origin/${METViewerBranch} -b "${METViewerBranch}-test"
    else
        # check to see if tag exists
        echo git rev-parse --verify --quiet ${METViewerTag}
        git rev-parse --verify --quiet ${METViewerTag}
        if [ $? -ne 0 ]; then
            echo "tag ${METViewerTag} does not exist in METViewer repository"
            usage
        fi
        git checkout tags/${METViewerTag} -b "${METViewerBranch}-test"
    fi
fi   # end if clone

cd $cdir

mkdir -p ${METViewerTestDir}
echo "clone the test cases repo"
if [ ! -d "${METViewerBranchTestDir}" ]; then
    mkdir -p ${METViewerBranchTestDir}
else
    rm -rf ${METViewerBranchTestDir}
fi
git clone https://${gituser}@github.com/NCAR/METViewer-test.git ${METViewerBranchTestDir}/test_data

# the test cases are on the unversioned master branch for now. checkout a local called test - just to be safe
cd ${METViewerBranchTestDir}/test_data
git checkout remotes/origin/master -b test
cd $cdir

# Fix up the test directory
#NOTE: The met load data 'met_data' is stored locally for now in $MET_DATA_DIR. Eventually this should also come from some common data repository
# and the mv_mysql.sql is delivered in the METViewer code
# and the mv_load.xml is checked into the test cases and must be moved here for the load test. It is needed for the load test but causes failures in mv_test where it is currently checked in
# This should probably all be straightened out a little but for now we fix it up here.
ln -sf ${MET_DATA_DIR}/met_data ${METViewerBranchTestDir}
mkdir -p ${METViewerBranchTestDir}/load_data/load/
cp ${METViewerDir}/sql/mv_mysql.sql  ${METViewerBranchTestDir}/load_data/load
cp ${METViewerBranchTestDir}/test_data/test_cases/loading/load_test.xml ${METViewerBranchTestDir}/load_data/load
rm -rf ${METViewerBranchTestDir}/test_data/test_cases/loading  # have to do this because it interferes with the test runner
ln -sf ${METViewerDir}/R_tmpl ${METViewerBranchTestDir}/R_tmpl
mkdir ${METViewerBranchTestDir}/R_work
mkdir ${METViewerBranchTestDir}/output
ln -sf ${METViewerDir}/R_work/include ${METViewerBranchTestDir}/R_work/include
mkdir ${METViewerBranchTestDir}/output/data
mkdir ${METViewerBranchTestDir}/output/plots
mkdir ${METViewerBranchTestDir}/output/scripts

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

# run the mv_test
#send a note
if [ "X$addressList" != "X" ]; then
	echo "running /bin/sh ./bin/mv_test.sh -t ${METViewerBranchTestDir} -m ${METViewerDir} -d ${mv_test_db} -u ${mv_user} -p ${mv_pass} -h ${mv_host} -P ${mv_port} -l -c> ${logfile}"
	/bin/sh ./bin/mv_test.sh -m${METViewerDir} -t${METViewerBranchTestDir} -d${mv_test_db} -u${mv_user} -p${mv_pass} -h${mv_host} -P${mv_port}  -l -c > ${logfile}
	ret=$?
	echo mv_test ret is $ret
	cat $logfile | mail -s "nightly_${METViewerBranch} mv_test failed with $ret failures - here is the log file" $addressList
else
	echo "running /bin/sh ./bin/mv_test.sh -t${METViewerBranchTestDir} -m${METViewerDir} -d${mv_test_db} -u${mv_user} -p${mv_pass} -h${mv_host} -P${mv_port} -l -c"
    /bin/sh  ./bin/mv_test.sh -m${METViewerDir} -t${METViewerBranchTestDir} -d${mv_test_db} -u${mv_user} -p${mv_pass} -h${mv_host} -P${mv_port} -l -c
	ret=$?
	echo mv_test ret is $ret
fi

# run the mv_compare
if [ "X$addressList" != "X" ]; then
	echo "running /bin/sh ./bin/mv_compare.sh  -m ${METViewerDir}  -t ${METViewerBranchTestDir} -c ${METViewerCompareBranchTestDir} > ${logfile}"
	/bin/sh  ./bin/mv_compare.sh  -m ${METViewerDir} -t${METViewerBranchTestDir} -c ${METViewerCompareBranchTestDir} > ${logfile}
	ret=$?
	echo mv_test ret is $ret
	cat $logfile | mail -s "nightly_${METViewerBranch} mv_compare failed with $ret failures - here is the log file" $addressList
else
	echo "running /bin/sh ./bin/mv_compare.sh  -m ${METViewerDir} -t ${METViewerBranchTestDir} -c ${METViewerCompareBranchTestDir}"
    /bin/sh  ./bin/mv_compare.sh  -m ${METViewerDir} -t ${METViewerBranchTestDir} -c ${METViewerCompareBranchTestDir}
	ret=$?
	echo mv_test ret is $ret
fi

exit $ret

	
