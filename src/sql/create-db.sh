#!/bin/sh

#
# $Id: create-db.sh,v 1.17 2003/02/01 02:15:33 jon Exp $
#

CMDNAME=`basename "$0"`
PATHNAME=`echo $0 | sed "s,$CMDNAME\$,,"`

# defaults
DB_SETTINGS="dbsettings.props"
DB_USER="${USER}"
DB_NAME='scarab'
DB_HOST='localhost'
DB_PORT='3306'
LOAD_ORDER="LoadOrder.lst"
POPULATION_SCRIPT_DIR='../../target/webapps/scarab/WEB-INF/sql'

# execute the settings file
if [ -f "${POPULATION_SCRIPT_DIR}/${DB_SETTINGS}" ] ; then
    . "${POPULATION_SCRIPT_DIR}/${DB_SETTINGS}"
fi

quiet=
EMPTY=

while [ "$#" -gt 0 ]
do
    case "$1" in
    --help|-\?)
        usage=t
        break
        ;;
    --username|-u)
        DB_USER="$2"
        shift;;
    --password|-p)
        password=t
        ;;
    --host|-h)
        DB_HOST="$2"
        shift;;
    --port|-P)
        DB_PORT="$2"
        shift;;
    --name|-n)
        DB_NAME="$2"
        shift;;
    --loadorder|-l)
        LOAD_ORDER="$2"
        shift;;
    --scripts|-s)
        POPULATION_SCRIPT_DIR="$2"
        shift;;
    --quiet|-q)
        quiet=t
        ;;
    --empty|-e)
        EMPTY=t
        ;;
    -*)
        echo "$CMDNAME: invalid option: $1" 1>&2
                echo "Try '$CMDNAME --help' for more information." 1>&2
        exit 1
        ;;
    *)
        ;;
    esac
    shift
done

####### Sanity checks
if [ ! -d "${POPULATION_SCRIPT_DIR}" ] ; then
    echo
    echo "The population script directory:"
    echo "${POPULATION_SCRIPT_DIR}"
    echo "does not exist. Please build Scarab first using the"
    echo "Ant build system as described in the scarab/README.txt file."
    echo "If Scarab is already built and you have defined a different"
    echo "context, then you must specifiy the -s option to this script"
    echo "to define the path to the directory."
    usage=t
fi

####### Sanity checks

if [ "${usage}" ] ; then
    echo
    echo "$CMDNAME creates a database and populates it with data."
    echo "             Currently only works with MySQL and Postgresql."
    echo
    echo "Usage:"
        echo "  $CMDNAME [options]"
        echo
    echo "Example:"
        echo "  $CMDNAME -h localhost -u scarab"
        echo        
    echo "Options:"
    echo "  -n, --name=DBNAME          Database name          (${DB_NAME})"
    echo "  -h, --host=HOSTNAME        Database server host   (${DB_HOST})"
    echo "  -P, --port=PORT            Database server port   (3306 M | 5432 P)"
    echo "  -u, --username=USERNAME    Username to connect as (${DB_USER})"
    echo "  -p, --password             Prompt for password"
    echo "  -l, --loadorder=FILE       SQL file load order    (${LOAD_ORDER})"
    echo "  -s, --scripts=DIR          SQL file directory"
    echo "                               (${POPULATION_SCRIPT_DIR})"
    echo "  -e, --empty                Create an empty database with only required data"
    echo "  -q, --quiet                Don't write any messages"
    echo "  -?, --help                 Usage"
    echo
    exit 0
fi

if [ -f "${POPULATION_SCRIPT_DIR}/mysql" ] ; then
    dbtype="mysql"
elif [ -f "${POPULATION_SCRIPT_DIR}/postgresql" ] ; then
    dbtype="postgresql"
fi

if [ -z "${quiet}" ] ; then
echo ""
echo "-----------------------------------------------------"
echo "Note: The .sql files are imported into the db from:"
echo "${POPULATION_SCRIPT_DIR}"
echo "If you change a .sql file in the scarab/src/sql directory,"
echo "then you need to make sure to re-run the Ant build system."
echo "-----------------------------------------------------"
echo ""
fi

# If user wants password, then...
if [ ! -z "$password" -a "${dbtype}" = 'mysql' ] ; then
    # Don't want to leave the user blind if he breaks
    # during password entry.
    trap 'stty echo >/dev/null 2>&1' 1 2 3 15
    
    # Check for echo -n vs echo \c
    if echo '\c' | grep -s c >/dev/null 2>&1
    then
        ECHO_N="echo -n"
        ECHO_C=""
    else
        ECHO_N="echo"
        ECHO_C='\c'
    fi

    $ECHO_N "Enter password for \"${DB_USER}\": "$ECHO_C
    stty -echo >/dev/null 2>&1
    read FirstPw
    stty echo >/dev/null 2>&1
    password="$FirstPw"
    echo
fi

if [ ! -z "${EMPTY}" ] ; then
    LOAD_ORDER="LoadOrder-Empty.lst"
fi

if [ -z "${quiet}" ] ; then
    echo "Importing sql files defined in ${LOAD_ORDER}..."
fi

###############
############### Mysql
###############
if [ "$dbtype" = 'mysql' ] ; then

MYSQL=`which mysql`
MYSQLSHOW=`which mysqlshow`
MYSQLADMIN=`which mysqladmin`

if [ ! -x "${MYSQL}" ] ; then
    echo
    echo "The MySQL binary needs to be in your PATH!"
    echo
    exit 1
fi

QUIETCMD=
if [ "${quiet}" = "t" ] ; then
    QUIETCMD="-s"
fi

PASSCMD=
if [ ! -z "${password}" ] ; then
    PASSCMD="--password=${password}"
fi

PORTCMD=
if [ "${DB_PORT}" != "" ] ; then
    PORTCMD="--port=${DB_PORT}"
fi
HOSTCMD="--host=${DB_HOST}"

USERCMD=
if [ "${DB_USER}" != "" ] ; then
    USERCMD="--user=${DB_USER}"
fi

MYSQLCMD="${QUIETCMD} ${HOSTCMD} ${PORTCMD} ${USERCMD} ${PASSCMD}"

# testing if the base already exists and removing it if needed.
base_exists=1
${MYSQLSHOW} ${MYSQLCMD} ${DB_NAME} > /dev/null 2>&1 || base_exists=0
if [ $base_exists -eq 1 ] ; then
    if [ -z "${quiet}" ] ; then
        echo "Removing existing database. All data will be lost."
    fi
    echo y | ${MYSQLADMIN} ${MYSQLCMD} drop ${DB_NAME} > /dev/null
fi

# Creating new database and inputting default data

if [ -z "${quiet}" ] ; then
    echo "Creating database ${DB_NAME}..."
fi
${MYSQLADMIN} ${MYSQLCMD} create ${DB_NAME}

FILES=`cat ${POPULATION_SCRIPT_DIR}/${LOAD_ORDER}`

for i in ${FILES} ; do
    if [ -z "${quiet}" ] ; then
        echo "Importing ${i}..."
    fi
    ${MYSQL} ${MYSQLCMD} ${DB_NAME} < ${POPULATION_SCRIPT_DIR}/${i}
done

###############
############### Postgresql
###############
elif [ "$dbtype" = 'postgresql' ] ; then

PSQL=`which psql`
PSQLCREATEDB=`which createdb`
PSQLDROPDB=`which dropdb`

if [ ! -x "${PSQL}" ] ; then
    echo
    echo "The psql binary needs to be in your PATH!"
    echo
    exit 1
fi

QUIETCMD=
if [ "${quiet}" = "t" ] ; then
    QUIETCMD="-q"
fi

PASSCMD=
if [ ! -z "${password}" ] ; then
    PASSCMD="-W"
fi

PORTCMD=
if [ "${DB_PORT}" != "" ] ; then
    PORTCMD="-p ${DB_PORT}"
fi
HOSTCMD="-h ${DB_HOST}"

USERCMD=
if [ "${DB_USER}" != "" ] ; then
    USERCMD="-U ${DB_USER}"
fi

PSQLCMD="${QUIETCMD} ${HOSTCMD} ${PORTCMD} ${USERCMD} ${PASSCMD}"

# drop the database
${PSQLDROPDB} ${PSQLCMD} ${DB_NAME}

# Creating new database and inputting default data
if [ -z "${quiet}" ] ; then
    echo "Creating Database ${DB_NAME}..."
fi

# create the database
${PSQLCREATEDB} ${PSQLCMD} ${DB_NAME}

FILES=`cat ${LOAD_ORDER}`
for i in ${FILES} ; do
    if [ -z "${quiet}" ] ; then
        echo "Importing ${i}..."
        ${PSQL} -f ${POPULATION_SCRIPT_DIR}/${i} ${PSQLCMD} ${DB_NAME}
    else
        ${PSQL} -f ${POPULATION_SCRIPT_DIR}/${i} ${PSQLCMD} ${DB_NAME} 2> /dev/null
    fi
done


fi

exit 0
