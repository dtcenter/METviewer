#!/bin/sh

#need to ser up custom tmpdir for the database to install properly
TMPDIR=$HOME/tmp

if [ -d "$TMPDIR" ]
then
    echo "Directory $TMPDIR exists."
else
    mkdir  $TMPDIR
    echo "Directory $TMPDIR is created."
fi


export TMPDIR

DATADIR="/var/lib/mysql"
MYSQL_ROOT_PASSWORD='mvuser'
tempSqlFile=$HOME/mysql-first-time.sql


echo 'Running mysql_install_db ..........'
mysql_install_db --datadir="$DATADIR"
echo 'Finished mysql_install_db ..........'

# create database init file in the home directory
# this file initialises root uset
cat > "$tempSqlFile" <<-EOSQL
			DELETE FROM mysql.user ;
			CREATE USER 'root'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD}' ;
			GRANT ALL ON *.* TO 'root'@'%' WITH GRANT OPTION ;
			DROP DATABASE IF EXISTS test ;
		EOSQL
echo 'FLUSH PRIVILEGES ;' >> "$tempSqlFile"


# start database with created init fire
echo 'Starting MariaDB  ..........'
exec mysqld_safe --init-file="$tempSqlFile" &

# set up Java
JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.362.b08-1.el7_9.x86_64"
export JAVA_HOME="$JAVA_HOME"
export JRE_HOME="$JAVA_HOME/jre"

# start Tomcat
echo 'Starting Tomcat  ..........'
exec /opt/tomcat/bin/startup.sh &
