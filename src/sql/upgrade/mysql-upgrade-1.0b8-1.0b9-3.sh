#!/bin/sh

### DEFINE THESE PARAMETERS
username=
password=
database=

HELP=$1

if [ "${HELP}" = "" ] ; then
echo
echo "This MySQL specific script does the following:"
echo 
echo "   1) Gets a list of deleted attributes"
echo "   2) Deletes the deleted attributes from the SCARAB_R_MODULE_ATTRIBUTE table"
echo "   3) Gets a list of options who's attributes have been deleted"
echo "   4) Deletes the options from the SCARAB_R_MODULE_OPTION table"
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

SQL="select SCARAB_ATTRIBUTE.ATTRIBUTE_ID from SCARAB_ATTRIBUTE where deleted=1"
echo $SQL |  ${MYSQL} -u{$username} -p{$password} {database} | grep -v '^ATTRIBUTE_ID' | \
while read attributeid ; do
 echo "attributeid is: $attributeid"
 SQL="delete from SCARAB_R_MODULE_ATTRIBUTE where ATTRIBUTE_ID='$attributeid'"
 echo $SQL |  ${MYSQL} -u{$username} -p{$password} {database} 
 SQL="select SCARAB_ATTRIBUTE_OPTION.OPTION_ID from SCARAB_ATTRIBUTE_OPTION where ATTRIBUTE_ID='$attributeid'"
 echo $SQL |  ${MYSQL} -u{$username} -p{$password} {database} | grep -v '^OPTION_ID' | \
 while read optionid; do
   echo "id is $optionid"
   SQL="delete from SCARAB_R_MODULE_OPTION where OPTION_ID='$optionid'"
   echo $SQL |  ${MYSQL} -u{$username} -p{$password} {database}
 done
done
