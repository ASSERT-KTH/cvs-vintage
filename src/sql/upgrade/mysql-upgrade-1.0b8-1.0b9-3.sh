#!/bin/sh

### DEFINE THESE PARAMETERS IF YOU NEED TO
username=
password=
HOSTNAME=
port=
database=scarab

HELP=$1

if [ "${HELP}" = "" ] ; then
echo
echo "This MySQL specific script does the following:"
echo 
echo "   1) Gets a list of deleted issue types"
echo "   2) Deletes the entries from SCARAB_R_MODULE_ISSUE_TYPES,
            SCARAB_R_MODULE_ATTRIBUTES, and SCARAB_R_MODULE_OPTIONS
            with ths issue type"
echo "   3) Gets a list of deleted attributes"
echo "   4) Deletes the deleted attributes from the SCARAB_R_MODULE_ATTRIBUTE table"
echo "   5) Gets a list of options who's attributes have been deleted"
echo "   6) Deletes the options from the SCARAB_R_MODULE_OPTION table"
echo
echo "In order to use this script, you must first edit it and define"
echo "the username, password and database variables at the top of the"
echo "script. Then, you must execute it like this:"
echo
echo "   ./mysql-upgrade-1.0b8-1.0b9-3.sh go"
echo
exit 1
fi

MYSQL=`which mysql`

if [ ! -x "${MYSQL}" ] ; then
    echo
    echo "The MySQL binary needs to be in your PATH!"
    echo
    exit 1
fi

USERCMD=
if [ "${username}" != "" ] ; then
    USERCMD="--user=${username}"
fi

PASSCMD=
if [ ! -z "${password}" ] ; then
    PASSCMD="--password=${password}"
fi
PORTCMD=
if [ "${port}" != "" ] ; then
    PORTCMD="--port=${port}"
fi
HOSTCMD="--host=${HOSTNAME}"

MYSQLCMD="${HOSTCMD} ${PORTCMD} ${USERCMD} ${PASSCMD}"

## for deleted issue types, delete module-issuetype mappings, attribute groups and attributes
SQL="select SCARAB_ISSUE_TYPE.ISSUE_TYPE_ID from SCARAB_ISSUE_TYPE where deleted=1"
echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} | grep -v '^ISSUE_TYPE_ID' | \
while read issuetypeid ; do
 echo "issuetypeid is: $issuetypeid"
 SQL="delete from SCARAB_R_MODULE_ISSUE_TYPE where ISSUE_TYPE_ID='$issuetypeid'"
 echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} 
 SQL="delete from SCARAB_ATTRIBUTE_GROUP where ISSUE_TYPE_ID='$issuetypeid'"
 echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} 
 SQL="delete from SCARAB_R_MODULE_ATTRIBUTE where ISSUE_TYPE_ID='$issuetypeid'"
 echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} 
 SQL="delete from SCARAB_R_MODULE_OPTION where ISSUE_TYPE_ID='$issuetypeid'"
 echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} 
done

## for deleted attributes, delete module-attribute mappings and module-option mappings

SQL="select SCARAB_ATTRIBUTE.ATTRIBUTE_ID from SCARAB_ATTRIBUTE where deleted=1"
echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} | grep -v '^ATTRIBUTE_ID' | \
while read attributeid ; do
 echo "attributeid is: $attributeid"
 SQL="delete from SCARAB_R_MODULE_ATTRIBUTE where ATTRIBUTE_ID='$attributeid'"
 echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} 
 SQL="select SCARAB_ATTRIBUTE_OPTION.OPTION_ID from SCARAB_ATTRIBUTE_OPTION where ATTRIBUTE_ID='$attributeid'"
 echo $SQL | ${MYSQL} ${MYSQLCMD} ${database} | grep -v '^OPTION_ID' | \
 while read optionid; do
   echo "optionid is $optionid"
   SQL="delete from SCARAB_R_MODULE_OPTION where OPTION_ID='$optionid'"
   echo $SQL | ${MYSQL} ${MYSQLCMD} ${database}
 done
done
