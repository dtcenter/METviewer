#!/bin/bash
export usage="USAGE: $0 [-u metViewerUrl] [-f feature] [-o outputFormat json|progress|snippets|summary[:outputFilePath]] \n\
[-b browser chrome|phantomjs|safari|firefox] [-n featureName (can be regex)] [-s step] [-p n] [-h help] [feature] \n \
where metViewerUrl default is 'http://www.dtcenter.org/met/metviewer/metviewer1.jsp,' \n \
-p is for pause after plot n is number of seconds.
feature default is 'all', outputFormat default is 'summary' outputFilePath default is stdout, and browser default is 'chrome'.\n
Phantonjs is a headless browser used for batch testing
Formats: \n
json: prints results in json format.\n
progress: appends one '.' for each feature as they run - gives a summary.\n
snippets:  prints only the code snippets for undefined steps.\n
summary: prints summary after all the scenarios are executed."

export metViewerUrl="http://www.dtcenter.org/met/metviewer/metviewer1.jsp"
export feature="all"
export outputFormat="pretty"
export browser="chrome"
export name=""
while getopts "u:f:o:b:n:s:p:" o; do
    case "${o}" in
        s)
            step="--step=${OPTARG}"
            ;;
        u)
            metViewerUrl=${OPTARG}
            ;;
        f)  feature=${OPTARG}
            ;;
        o)  case ${OPTARG} in
                json|progress|snippets|summary) feature=${OPTARG};;
                *) echo feature is not valid, must be json|progress|snippets|summary
                    echo -e $usage
                    exit 1
                    ;;
           esac
            ;;
        b) case ${OPTARG} in
            chrome|phantomjs|safari|firefox) browser=${OPTARG};;
            *) echo browser is not valid, must be chrome|phantomjs|safari|firefox
                echo -e $usage
                exit 1
                ;;
           esac
        ;;
        n)  name=${OPTARG}
            ;;
        p) pauseAfterPlot="--pauseAfterPlot=${OPTARG}"
            ;;
        *)
            echo -e $usage
            echo "available features are:"
            find features -maxdepth 1 -type d -not -path "." -not -path features/common -not -path features | while read x;
            do
                feature=`basename $x`;
                echo $feature
            done
            exit 1
            ;;
    esac
done
shift $((OPTIND-1))


if [[ $# -ne 0 ]]; then
            echo -e $usage
            echo "available features are:"
            find features -maxdepth 1 -type d -not -path "." -not -path features/common -not -path features/googleTest | while read x;
            do
                feature=`basename $x`;
                echo $feature
            done
            exit 1
fi

if [ -z ${name+x} ] ; then
        echo "./node_modules/chimp/bin/chimp.js --test --chai --metViewerUrl=${metViewerUrl} features/${runFeature}  --format=${outputFormat} ${step} ${pauseAfterPlot}"
        ./node_modules/chimp/bin/chimp.js --test --chai --metViewerUrl=${metViewerUrl} --format=${outputFormat} --name=${name} -${step} ${pauseAfterPlot}
else
    for featureName in $(find features -maxdepth 1 -type d -not -path "." -not -path features/common -not -path features)
    do
        runFeature=`basename $featureName`
        if  [[ ${runFeature} != "${feature}" ]]; then
            if [[ ${feature} != "all" ]]; then
             continue
            fi
        fi
        echo "running feature common against metViewerUrl ${metViewerUrl}"
        echo "./node_modules/chimp/bin/chimp.js --test --chai  --metViewerUrl=${metViewerUrl} --format=${outputFormat} --browser=${browser} features/common ${step} ${pauseAfterPlot}"
        ./node_modules/chimp/bin/chimp.js --test --chai  --metViewerUrl=${metViewerUrl} features/common  --format=${outputFormat} ${step} ${pauseAfterPlot}
        echo "running feature ${runFeature} against metViewerUrl ${metViewerUrl}"
        echo "./node_modules/chimp/bin/chimp.js --test --chai --metViewerUrl=${metViewerUrl} features/${runFeature}  --format=${outputFormat} ${step} ${pauseAfterPlot}"
        ./node_modules/chimp/bin/chimp.js --test --chai --metViewerUrl=${metViewerUrl} features/${runFeature}  --format=${outputFormat} ${step} ${pauseAfterPlot}
    done
fi
