$username=
$password=
$database=

SQL="select SCARAB_ATTRIBUTE.ATTRIBUTE_ID from SCARAB_ATTRIBUTE where deleted=1"

IFS=`        `
echo $SQL |  mysql -u{$username} -p{$password}   {database} | grep -v '^ATTRIBUTE_ID' | \
while read attributeid ; do
 echo "id is $attributeid"
 SQL="delete from SCARAB_R_MODULE_ATTRIBUTE where ATTRIBUTE_ID= '$attributeid'"
 echo $SQL |  mysql -u{$username} -p{$password}   {database} 
 SQL="select SCARAB_ATTRIBUTE_OPTION.OPTION_ID from SCARAB_ATTRIBUTE_OPTION where ATTRIBUTE_ID= '$attributeid'"
 echo $SQL |  mysql -u{$username} -p{$password}   {database} | grep -v '^OPTION_ID' | \
 while read optionid; do
   echo "id is $optionid"
   SQL="delete from SCARAB_R_MODULE_OPTION where OPTION_ID= '$optionid'"
   echo $SQL |  mysql -u{$username} -p{$password}   {database} 
 done  
done
