#!/bin/sh

#
# $Id: create-db.sh,v 1.21 2003/04/07 17:40:05 jon Exp $
#

CMDNAME=`basename "$0"`
PATHNAME="`dirname ${0} 2> /dev/null`"
if [ -z "${PATHNAME}" -o ! -d "${PATHNAME}" ]; then
    PATHNAME='.'
fi

# defaults
DB_SETTINGS="dbsettings.props"
DB_USER="${USER}"
DB_NAME='scarab'
DB_HOST='localhost'
DB_PORT='3306'
LOAD_ORDER="LoadOrder.lst"
POPULATION_SCRIPT_DIR="${PATHNAME}/../../target/webapps/scarab/WEB-INF/sql"

# execute the settings file
if [ -f "${POPULATION_SCRIPT_DIR}/${DB_SETTINGS}" ] ; then
    OLD_PS_DIR="${POPULATION_SCRIPT_DIR}"
    . "${POPULATION_SCRIPT_DIR}/${DB_SETTINGS}"
    if [ ! -d "${POPULATION_SCRIPT_DIR}" ]; then
        POPULATION_SCRIPT_DIR="${OLD_PS_DIR}"
    fi
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
    echo "  -n, --name DBNAME          Database name          (${DB_NAME})"
    echo "  -h, --host HOSTNAME        Database server host   (${DB_HOST})"
    echo "  -P, --port PORT            Database server port   (3306 M | 5432 P)"
    echo "  -u, --username USERNAME    Username to connect as (${DB_USER})"
    echo "  -p, --password             Prompt for password"
    echo "  -l, --loadorder FILE       SQL file load order    (${LOAD_ORDER})"
    echo "  -s, --scripts DIR          SQL file directory"
    echo "                               (${POPULATION_SCRIPT_DIR})"
    echo "  -e, --empty                Create an empty database with only required data"
    echo "  -q, --quiet                Don't write any messages"
    echo "  -?, --help                 Usage"
    echo
    exit 0
fi

## Verify that we have a valid looking password...
if [ -z "${DB_USER}" ] ; then
    echo "WARNING: There is no username specified for the database."
    echo "If you get access denied errors, you can specify one"
    echo "on the command line with the -u option or by editing your"
    echo "build.properties file and specifying the scarab.database.username"
    echo "property.  You may also want to use the -p option to force a "
    echo "prompt for the password or specify the scarab.database.password"
    echo "in your build.properties."
fi

if [ -f "${POPULATION_SCRIPT_DIR}/mysql" ] ; then
    dbtype="mysql"
elif [ -f "${POPULATION_SCRIPT_DIR}/postgresql" ] ; then
    dbtype="postgresql"
elif [ -f "${POPULATION_SCRIPT_DIR}/oracle" ] ; then 
    dbtype="oracle"
fi

if [ -z "${quiet}" ] ; then
echo
echo "-----------------------------------------------------"
echo "Note: The .sql files are imported into the db from:"
echo "${POPULATION_SCRIPT_DIR}"
echo "If you change a .sql file in the scarab/src/sql directory,"
echo "then you need to make sure to re-run the Ant build system."
echo "-----------------------------------------------------"
echo
fi

# If user wants password, then...
if [ ! -z "$password"  ] ; then
    if [ "${dbtype}" = 'mysql' -o "${dbtype}" = 'oracle' ] ; then 
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

elif [ "$dbtype" = "oracle" ] ; then 

echo "Loading SQL into Oracle"

# make sure some things are setup for oracle 
if [ -z "$ORACLE_HOME" ] ; then
    echo 
    echo "ORACLE_HOME must be defined before executing this script."
    echo "This is usually done by sourcing 'oraenv' into your environment."
    echo 
    exit 1
fi

if [ -z "$ORACLE_SID" ] ; then 
    echo 
    echo "ORACLE_SID must be defined before executing this script."
    echo "This is usually done by sourcing 'oraenv' into your environment."
    echo 
fi

SQLPLUS=$ORACLE_HOME/bin/sqlplus 
if [ ! -x $SQLPLUS ] ; then 
    echo 
    echo "sqlplus could not be found off $ORACLE_HOME."
    echo "Please check your Oracle installation."
    echo 
fi 

if [ -z "$LD_LIBRARY_PATH" ] ; then 
    LD_LIBRARY_PATH=$ORACLE_HOME/lib32:$ORACLE_HOME/lib  
else
    LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$ORACLE_HOME/lib32:$ORACLE_HOME/lib  
fi 
export LD_LIBRARY_PATH 

if [ -f /tmp/${PPID}exit.sql ] ; then 
    rm -f /tmp/${PPID}exit.sql 
fi 
echo "exit" > /tmp/${PPID}exit.sql 

FILES=`cat ${LOAD_ORDER}`
for i in ${FILES} ; do 
    
    cat $i /tmp/${PPID}exit.sql > /tmp/${PPID}sql.sql 

    if [ -z "${quiet}" ] ; then 
        ${SQLPLUS} ${DB_USER}/${password} @/tmp/${PPID}sql.sql 
    else 
        ${SQLPLUS} ${DB_USER}/${password} @/tmp/${PPID}sql.sql  2> /dev/null 
    fi 

    rm -f /tmp/${PPID}sql.sql 
done
rm -f /tmp/${PPID}exit.sql 

fi #end if db = 'foo'

exit 0
