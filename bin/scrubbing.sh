#!/bin/bash

## script name; mysql_script_runner.sh
## wrapper script to execute mysql script with variables

ARGS=5

if [ "$ARGS" -gt $# ]
then

 echo "you passed $# parameters"
 echo "Usage: `basename $0` sql_script_file  database_name field_name min_value max_value info (optional)"

exit
fi

sql_script=$1
database=$2
field_name=$3
min_value=$4
max_value=$5
#info_only=$6
info_only="false"

if [[ $6 == "info" ]];
then
  info_only="true"
fi


OLD_FIELD="_field_name_"
OLD_DATABASE="_database_name_"
OLD_INFO="_info_only_"
BPATH="temp.sql"

if [ -f ${sql_script} -a -r ${sql_script} ];
then
  #replace variables in the script and save it to the temp file
  sed -e "s/$OLD_FIELD/${field_name}/g;s/$OLD_DATABASE/${database}/g;s/$OLD_INFO/${info_only}/g" "${sql_script}" > $BPATH
else
  echo "Error: Cannot read ${sql_script}"
fi


#run mysql query with parameters
mysql -umvuser -pmvuser ${database} -A -e " set @min_value=${min_value}; set @max_value=${max_value};  source ${BPATH};"

#remove temp file
rm ${BPATH}

exit

# end of script.

#./scrubbing.sh /d3/projects/METViewer/src/apps/verif/metviewer/sql/scrubbing.sql  mv_tatiana fcst_valid_beg '"2010-06-01 12:00:00"' '"2010-06-01 12:00:00"' info
#./scrubbing.sh /d3/projects/METViewer/src/apps/verif/metviewer/sql/scrubbing.sql  mv_tatiana fcst_valid_end '"2010-06-01 12:00:00"' '"2010-06-01 12:00:00"'
#./scrubbing.sh /d3/projects/METViewer/src/apps/verif/metviewer/sql/scrubbing.sql  mv_tatiana f_rate 0 0.1 info
#./scrubbing.sh /d3/projects/METViewer/src/apps/verif/metviewer/sql/scrubbing.sql  mv_tatiana f_rate 0 0.1