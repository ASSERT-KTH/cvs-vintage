#!/bin/sh

# The directory which the SQL scripts have been pre-processed into
POPULATION_SCRIPT_DIR='../../target/webapps/scarab/WEB-INF/sql'
# The settings file for defining values.
DB_SETTINGS='dbsettings.props'

# Sanity check
if [ ! -d "${POPULATION_SCRIPT_DIR}" ] ; then
    echo "The population script directory:"
    echo "${POPULATION_SCRIPT_DIR}"
    echo "does not exist. Please build Scarab first using the"
    echo "Ant build system as described in the scarab/README.txt file."
    exit 1
fi

echo ""
echo "-----------------------------------------------------"
echo "Note: The .sql files are imported into the db from:"
echo "${POPULATION_SCRIPT_DIR}"
echo "If you change a .sql file in the scarab/src/sql directory,"
echo "then you need to make sure to re-run the Ant build system."
echo "-----------------------------------------------------"
echo ""

# Evaluate the settings file (if we have one)
if [ -f "${DB_SETTINGS}" ] ; then
    . "./${DB_SETTINGS}"
fi

# Defaults database settings can be defined in the $DB_SETTINGS file
# and overridden on the command line
if [ "$1" != '' ] ; then
    USER="$1"
elif [ "$USER" = '' ] ; then
    USER=`whoami`
fi
if [ "$2" != '' ] ; then
    PASS="$2"
elif [ "$PASS" = '' ] ; then
    PASS=''
fi
if [ "$3" != '' ] ; then
    DB_NAME="$3"
elif [ "$DB_NAME" = '' ] ; then
    DB_NAME='scarab'
fi
if [ "$4" != '' ] ; then
    LOAD_ORDER="$4"
elif [ "$LOAD_ORDER" = '' ] ; then
    LOAD_ORDER="${POPULATION_SCRIPT_DIR}/LoadOrder.lst"
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

# Creating new database and inputting default data

echo "Creating Database ${DB_NAME}..."
${MYSQLADMIN} -u ${USER} ${PASSCMD} create ${DB_NAME}

FILES=`cat ${LOAD_ORDER}`

for i in ${FILES} ; do
    echo "Importing ${i}..."
    ${MYSQL} -u ${USER} ${PASSCMD} ${DB_NAME} < ${POPULATION_SCRIPT_DIR}/${i}
done
