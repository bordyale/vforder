#!/bin/bash
# Shell script to backup MySql database
# To backup Nysql databases file to /backup dir and later pick up by your
# script. You can skip few databases from backup too.
# For more info please see (Installation info):
# http://www.cyberciti.biz/nixcraft/vivek/blogger/2005/01/mysql-backup-script.html
# Last updated: Aug - 2005
# --------------------------------------------------------------------
# This is a free shell script under GNU GPL version 2.0 or above
# Copyright (C) 2004, 2005 nixCraft project
# -------------------------------------------------------------------------
# This script is part of nixCraft shell script collection (NSSC)
# Visit http://bash.cyberciti.biz/ for more information.
# -------------------------------------------------------------------------
 
if [ $# -ne 4 ]
then
		echo "Usage: $0 {MySQL-User-Name} {MySQL-User-Password} {backupDir} {backup name ex: osboxes.YYYY-MM-DD--HH-MM}"
		echo "restore Ofbiz DB from backup"
		echo "Example: $0 XXX XXX /home/ale/Dropbox/backupMySql/mysql osboxes.2019-09-23--13-36"
				exit 1
			fi



MyUSER=${1}     # USERNAME
MyPASS=${2}   # PASSWORD
DEST=${3}
BACKUPNAME=${4}
MyHOST="localhost"        # Hostname
 
# Linux bin paths, change this if it can't be autodetected via which command
MYSQL="$(which mysql)"
MYSQLDUMP="$(which mysqldump)"
CHOWN="$(which chown)"
CHMOD="$(which chmod)"
GZIP="$(which gzip)"
GUNZIP="$(which gunzip)"
 
# Backup Dest directory, change this if you have someother location
#DEST="${HOME}/Dropbox/backupMySql"
 
# Main directory where backup will be stored
MBD="$DEST/mysql"
 
# Get hostname
HOST="$(hostname)"
 
# Get data in dd-mm-yyyy format
NOW="$(date +"%Y-%m-%d--%H-%M")"
 
# File to store current backup file
FILE=""
# Store list of databases
DBS=""
 
# DO NOT BACKUP these databases
IGGY="test information_schema mysql performance_schema"
 

DBS="$($MYSQL -u $MyUSER -h $MyHOST -p$MyPASS -Bse 'show databases')"

echo "Databases before delete:" 
echo $DBS
 
# Get all database list first
$MYSQL -u $MyUSER -h $MyHOST -p$MyPASS -Bse 'drop database ofbiz'
$MYSQL -u $MyUSER -h $MyHOST -p$MyPASS -Bse 'drop database ofbiztenant'
$MYSQL -u $MyUSER -h $MyHOST -p$MyPASS -Bse 'drop database ofbizolap'

DBS="$($MYSQL -u $MyUSER -h $MyHOST -p$MyPASS -Bse 'show databases')"
echo "Databases after delete:" 
echo $DBS

echo "Create new databases:"

$MYSQL -u $MyUSER -h $MyHOST -p$MyPASS << EOF
create database ofbiz;
create database ofbizolap;
create database ofbiztenant;
use mysql;
select database();
#create user ofbiz@localhost;
#create user ofbizolap@localhost;
#create user ofbiztenant@localhost;
update user set password=PASSWORD("ofbiz") where User='ofbiz';
update user set password=PASSWORD("ofbizolap") where User='ofbizolap';
update user set password=PASSWORD("ofbiztenant") where User='ofbiztenant';
grant all privileges on *.* to 'ofbiz'@localhost identified by 'ofbiz';
grant all privileges on *.* to 'ofbizolap'@localhost identified by 'ofbizolap';
grant all privileges on *.* to 'ofbiztenant'@localhost identified by 'ofbiztenant';
EOF

DBS="$($MYSQL -u $MyUSER -h $MyHOST -p$MyPASS -Bse 'show databases')"
echo "Databases after create:"
echo $DBS

OFBIZDB="${DEST}/ofbiz.${BACKUPNAME}"
OFBIZTENANTDB="${DEST}/ofbiztenant.${BACKUPNAME}"
OFBIZOLAPDB="${DEST}/ofbizolap.${BACKUPNAME}"
echo $OFBIZDB
echo $OFBIZTENANTDB
echo $OFBIZOLAPDB
$GUNZIP "${OFBIZDB}.gz"
$GUNZIP "${OFBIZTENANTDB}.gz"
$GUNZIP "${OFBIZOLAPDB}.gz"

$MYSQL -u $MyUSER -h $MyHOST -p$MyPASS << EOF
use ofbiz
\. $OFBIZDB
use ofbiztenant
\. $OFBIZTENANTDB
use ofbizolap
\. $OFBIZOLAPDB
EOF
