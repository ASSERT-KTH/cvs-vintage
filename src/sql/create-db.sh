#!/bin/sh

#
# $Id: create-db.sh,v 1.1 2002/02/28 02:01:10 jon Exp $
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
    --password|-W)
        password=t
        ;;
    --host|-h)
        HOSTNAME="$2"
        shift;;
    --port|-p)
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
        dbtype="$1"
        ;;
    esac
    shift
done

####### Sanity checks
if [ "$dbtype" != "mysql" -a "$dbtype" != "postgresql" ] ; then
    echo
    echo "Please specify either 'mysql' or 'postgresql'"
    usage=t
fi

if [ ! -d "${POPULATION_SCRIPT_DIR}" ] ; then
    echo
    echo "The population script directory:"
    echo "${POPULATION_SCRIPT_DIR}"
    echo "does not exist. Please build Scarab first using the"
    echo "Ant build system as described in the scarab/README.txt file."
    usage=t
fi
####### Sanity checks

if [ "$usage" ] ; then
        echo
        echo "$CMDNAME creates a database and populates it with data."
        echo "             Currently only works with MySQL and Postgresql."
        echo
    echo "Usage:"
        echo "  $CMDNAME [options] (mysql | postgresql)"
        echo
    echo "Example:"
        echo "  $CMDNAME -h localhost -u scarab mysql"
        echo        
    echo "Options:"
    echo "  -n, --name=DBNAME               Database name          (${name})"
    echo "  -h, --host=HOSTNAME             Database server host   (localhost)"
    echo "  -p, --port=PORT                 Database server port   (3306 M | 5432 P)"
    echo "  -u, --username=USERNAME         Username to connect as (${username})"
    echo "  -W, --password                  Prompt for password"
    echo "  -l, --loadorder=FILE            SQL file load order    (LoadOrder.lst)"
    echo "  -s, --scripts=DIR               SQL file directory"
    echo "                                    (${POPULATION_SCRIPT_DIR})"
    echo "  -q, --quiet                     Don't write any messages"
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
if [ ! -z "$password" ] ; then
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
    exit 0
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
    port=5432
    echo
    echo "NOT YET IMPLEMENTED!"
    echo
fi

exit 0
