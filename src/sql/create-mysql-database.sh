#!/bin/sh

# The directory which the SQL scripts have been pre-processed into
POPULATION_SCRIPT_DIR='../../target/webapps/scarab/WEB-INF/sql'

# Sanity check
if [ ! -d "${POPULATION_SCRIPT_DIR}" ] ; then
    echo "The population script directory:"
    echo "'${POPULATION_SCRIPT_DIR}'"
    echo "does not exist. Please build Scarab first using the"
    echo "ant build system as described in the scarab/README.txt file."
    exit 1
fi

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
if [ "$3" != "" ] ; then
    DB_NAME=$3
else
    DB_NAME=scarab
fi
if [ "$4" != "" ] ; then
    LOAD_ORDER=$4
else
    LOAD_ORDER=${POPULATION_SCRIPT_DIR}/LoadOrder.lst
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
${MYSQLSHOW} -u ${USER} ${PASSCMD} ${DB_NAME} > /dev/null 2>&1 || base_exists=0
if [ $base_exists -eq 1 ] ; then
	echo "Removing existing database. All data will be lost."
        echo y | ${MYSQLADMIN} -u ${USER} ${PASSCMD} drop ${DB_NAME} > /dev/null
fi

# Creating new base and inputting default data

echo "Creating Database ${DB_NAME}..."
${MYSQLADMIN} -u ${USER} ${PASSCMD} create ${DB_NAME}

FILES=`cat ${LOAD_ORDER}`

for i in ${FILES} ; do
    echo "Importing ${i}..."
    ${MYSQL} -u ${USER} ${PASSCMD} ${DB_NAME} < ${POPULATION_SCRIPT_DIR}/${i}
done
