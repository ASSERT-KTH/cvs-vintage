#!/bin/sh

# Define these values if you need to
if [ "$1" != "" ] ; then
    USER=$1
else
    USER=`whoami`
fi
if [ "$2" != "" ] ; then
    PASS=$2
else
    PASS=
fi

MYSQL=`which mysql`
MYSQLSHOW=`which mysqlshow`
MYSQLADMIN=`which mysqladmin`

if [ ! -x "${MYSQL}" ] ; then
    echo "The MySQL binary needs to be in your PATH!"
    exit
fi

if [ "${PASS}" != "" ] ; then
    PASSCMD="-p${PASS}"
fi

# testing if the base already exists and removing it if needed.
base_exists=1
${MYSQLSHOW} -u ${USER} ${PASSCMD} scarab > /dev/null 2>&1 || base_exists=0
if [ $base_exists -eq 1 ] ; then
	echo "Removing existing database. All data will be lost."
        echo y | ${MYSQLADMIN} -u ${USER} ${PASSCMD} drop scarab > /dev/null
fi

# Creating new base and inputting default data

echo "Creating Database..."        
${MYSQLADMIN} -u ${USER} ${PASSCMD} create scarab

echo "Importing mysql-scarab.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-scarab.sql

echo "Importing mysql-turbine.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-turbine.sql

echo "Importing mysql-id-table-schema.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-id-table-schema.sql

echo "Importing mysql-turbine-id-table-init.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-turbine-id-table-init.sql

echo "Importing mysql-turbine-security.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-turbine-security.sql

echo "Importing mysql-scarab-id-table-init.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-scarab-id-table-init.sql

echo "Importing mysql-scarab-default-data.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-scarab-default-data.sql

echo "Importing mysql-scarab-sample-data.sql..."
${MYSQL} -u ${USER} ${PASSCMD} scarab < mysql-scarab-sample-data.sql
