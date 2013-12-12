#!/bin/bash
#
# trap handler: print location of last error and process it further
#
function my_trap_handler()
{
        MYSELF="$0"               # equals to my script name
        LASTLINE="$1"            # argument 1: last line of error occurence
        LASTERR="$2"             # argument 2: error code of last command
        printf "\n${MYSELF}: line ${LASTLINE}: exit status of last command: ${LASTERR}"

        # do additional processing: send email or SNMP trap, write result to database, etc.
}
#
# trap commands with non-zero exit code
#
trap 'my_trap_handler ${LINENO} $?' ERR

SUBJECT="Automated message from the DB backup on mandan"
EMAIL="tatiana@ucar.edu"
ERRORMESSAGE="/d3/projects/METViewer/src/apps/verif/metviewer/bin/errorMessage.txt"
SUCCESSMESSAGE="/d3/projects/METViewer/src/apps/verif/metviewer/bin/successMessage.txt"

MAIN_START=$(date +%s.%N)
mysql -umvuser -pmvuser -e 'show databases;' | {
	while read db_name ; do
		if [ "$db_name" != "information_schema" -a  "$db_name" != "Database"  -a  "$db_name" != "mysql" ];
		then
		  printf '\n\nAbout to back up database: '$db_name
			LAST_DATE_DEV_STR=$(mysql -umvuser -pmvuser -h dakota $db_name -e 'select max(load_date) from data_file ;' | sed -n '2p')
      printf '\nLast date from dev: '$LAST_DATE_DEV_STR
      LAST_DATE_PROD_STR=$(mysql -umvuser -pmvuser  $db_name -e 'select max(load_date) from data_file ;' | sed -n '2p')
      if [ "$LAST_DATE_PROD_STR" != "NULL" ];
      then
        #database is real and has data - check if it needs to be based up
        printf '\nLast date from prod: '$LAST_DATE_PROD_STR

        LAST_DATE_DEV=$(date -d "$LAST_DATE_DEV_STR" +%s)
        LAST_DATE_PROD=$(date -d "$LAST_DATE_PROD_STR" +%s)

        #compate dates of uploading
        if  [[ $LAST_DATE_DEV = "NULL" ||  $LAST_DATE_DEV -ne $LAST_DATE_PROD ]]  ;
        then
          #dates are different - changes were made - backup database

          #analyze it first
          mysqlcheck -a -umvuser -pmvuser $db_name

          mysqldump -umvuser -pmvuser $db_name > $db_name.sql
          mysql -umvuser -pmvuser -hdakota  $db_name <  $db_name.sql
          rm $db_name.sql
          if [ $? -gt 0 ];
          then
            #error detected during the backup - send email
            /usr/bin/mail -s "$SUBJECT" "$EMAIL" < $ERRORMESSAGE
          else
            #successful backup
            printf '\n'$db_name' was backed up'
            /usr/bin/mail -s "$SUBJECT" "$EMAIL" < $SUCCESSMESSAGE
          fi
        else
          #dates are the same - no changes were made - backup is not needed
          printf '\n'$db_name' was not changed and was not backed up'
        fi
      else
        #database is empty - no backup
        printf '\n'$db_name' does not have data No backup'
      fi
    fi
	done
}


MAIN_END=$(date +%s.%N)
MAIN_DIFF=$(echo "$MAIN_END - $MAIN_START" | bc)
printf '\n\nBackup took : '$MAIN_DIFF'seconds'

#Run backup script at 3 AM  everyday
# 00 9 * * * /d3/projects/METViewer/src/apps/verif/metviewer/bin/db_backup.sh > /d3/projects/METViewer/src/apps/verif/metviewer/bin/db_backup.log 2>&1
