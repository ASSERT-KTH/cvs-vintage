#!/bin/sh

#
# $Id: create-db.sh,v 1.8 2002/03/19 00:00:15 jon Exp $
#

CMDNAME=`basename "$0"`
PATHNAME=`echo $0 | sed "s,$CMDNAME\$,,"`

name='scarab'
loadorder='LoadOrder.lst'
POPULATION_SCRIPT_DIR='../../target/webapps/scarab/WEB-INF/sql'
username="${USER}"
HOSTNAME='localhost'
quiet=

while [ "$#" -gt 0 ]
do
    case "$1" in
    --help|-\?)
        usage=t
        break
        ;;
    --username|-u)
        username="$2"
        shift;;
    --password|-p)
        password=t
        ;;
    --host|-h)
        HOSTNAME="$2"
        shift;;
    --port|-P)
        port="$2"
        shift;;
    --name|-n)
        name="$2"
        shift;;
    --loadorder|-l)
        loadorder="$2"
        shift;;
    --scripts|-s)
        POPULATION_SCRIPT_DIR="$2"
        shift;;
    --quiet|-q)
        quiet=t
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

if [ -e "${POPULATION_SCRIPT_DIR}/mysql" ] ; then
    dbtype="mysql"
elif [ -e "${POPULATION_SCRIPT_DIR}/postgresql" ] ; then
    dbtype="postgresql"
fi

####### Sanity checks
if [ ! -d "${POPULATION_SCRIPT_DIR}" ] ; then
    echo
    echo "The population script directory:"
    echo "${POPULATION_SCRIPT_DIR}"
    echo "does not exist. Please build Scarab first using the"
    echo "Ant build system as described in the scarab/README.txt file."
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
    echo "  -n, --name=DBNAME          Database name          (${name})"
    echo "  -h, --host=HOSTNAME        Database server host   (localhost)"
    echo "  -P, --port=PORT            Database server port   (3306 M | 5432 P)"
    echo "  -u, --username=USERNAME    Username to connect as (${username})"
    echo "  -p, --password             Prompt for password"
    echo "  -l, --loadorder=FILE       SQL file load order    (LoadOrder.lst)"
    echo "  -s, --scripts=DIR          SQL file directory"
    echo "                               (${POPULATION_SCRIPT_DIR})"
    echo "  -q, --quiet                Don't write any messages"
    echo
    exit 0
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

	$ECHO_N "Enter password for \"${username}\": "$ECHO_C
    stty -echo >/dev/null 2>&1
    read FirstPw
    stty echo >/dev/null 2>&1
	password="$FirstPw"
    echo
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
if [ "${port}" != "" ] ; then
    PORTCMD="--port=${port}"
fi
HOSTCMD="--host=${HOSTNAME}"

USERCMD=
if [ "${username}" != "" ] ; then
    USERCMD="--user=${username}"
fi

MYSQLCMD="${QUIETCMD} ${HOSTCMD} ${PORTCMD} ${USERCMD} ${PASSCMD}"

# testing if the base already exists and removing it if needed.
base_exists=1
${MYSQLSHOW} ${MYSQLCMD} ${name} > /dev/null 2>&1 || base_exists=0
if [ $base_exists -eq 1 ] ; then
    if [ -z "${quiet}" ] ; then
        echo "Removing existing database. All data will be lost."
    fi
    echo y | ${MYSQLADMIN} ${MYSQLCMD} drop ${name} > /dev/null
fi

# Creating new database and inputting default data

if [ -z "${quiet}" ] ; then
    echo "Creating Database ${name}..."
fi
${MYSQLADMIN} ${MYSQLCMD} create ${name}

FILES=`cat ${loadorder}`

for i in ${FILES} ; do
    if [ -z "${quiet}" ] ; then
        echo "Importing ${i}..."
    fi
    ${MYSQL} ${MYSQLCMD} ${name} < ${POPULATION_SCRIPT_DIR}/${i}
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
if [ "${port}" != "" ] ; then
    PORTCMD="-p ${port}"
fi
HOSTCMD="-h ${HOSTNAME}"

USERCMD=
if [ "${username}" != "" ] ; then
    USERCMD="-U ${username}"
fi

PSQLCMD="${QUIETCMD} ${HOSTCMD} ${PORTCMD} ${USERCMD} ${PASSCMD}"

# drop the database
${PSQLDROPDB} ${PSQLCMD} ${name}

# Creating new database and inputting default data
if [ -z "${quiet}" ] ; then
    echo "Creating Database ${name}..."
fi

# create the database
${PSQLCREATEDB} ${PSQLCMD} ${name}

FILES=`cat ${loadorder}`
for i in ${FILES} ; do
    if [ -z "${quiet}" ] ; then
        echo "Importing ${i}..."
        ${PSQL} -f ${POPULATION_SCRIPT_DIR}/${i} ${PSQLCMD} ${name}
    else
        ${PSQL} -f ${POPULATION_SCRIPT_DIR}/${i} ${PSQLCMD} ${name} 2> /dev/null
    fi
done


fi

exit 0
