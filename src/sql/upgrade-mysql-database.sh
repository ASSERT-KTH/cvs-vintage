#!/bin/sh

DB_SETTINGS="dbsettings.props"
UPGRADE="upgrade"

# execute the settings file
if [ -f "${DB_SETTINGS}" ] ; then
    . "./${DB_SETTINGS}"
fi

if [ "$1" != "" ] ; then
    FROM=$1
else
    echo "Need to specify a FROM version."
    echo "Usage:   upgrade-mysql-database.sh FROM TO"
    echo "Example: upgrade-mysql-database.sh 1.0b1 1.0b2"
    exit
fi
if [ "$2" != "" ] ; then
    TO=$2
else
    echo "Need to specify a TO version."
    echo "Usage:   upgrade-mysql-database.sh FROM TO"
    echo "Example: upgrade-mysql-database.sh 1.0b1 1.0b2"
    exit
fi

MYSQL=`which mysql`
MYSQLSHOW=`which mysqlshow`

if [ ! -x "${MYSQL}" ] ; then
    echo "The MySQL binary needs to be in your PATH!"
    exit
fi

if [ "${PASS}" != "" ] ; then
    PASSCMD="-p${PASS}"
fi

# testing if the base already exists and removing it if needed.
base_exists=1
${MYSQLSHOW} -u ${USER} ${PASSCMD} ${DB_NAME} > /dev/null 2>&1 || base_exists=0
if [ $base_exists -ne 1 ] ; then
	echo "Error: Scarab database does not exist!"
	exit
fi

# Creating new base and inputting default data

for i in `ls ${UPGRADE}/mysql-upgrade-${FROM}-${TO}-*.sql` ; do
    echo "${i}..."
    ${MYSQL} -u ${USER} ${PASSCMD} ${DB_NAME} < ${i}    
done
