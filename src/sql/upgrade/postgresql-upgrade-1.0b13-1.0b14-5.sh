#!/bin/sh

CMDNAME=`basename "$0"`
DB_SETTINGS="dbsettings.props"
POPULATION_SCRIPT_DIR='../../../target/webapps/scarab/WEB-INF/sql'

# execute the settings file
if [ -f "${POPULATION_SCRIPT_DIR}/${DB_SETTINGS}" ] ; then
    . "${POPULATION_SCRIPT_DIR}/${DB_SETTINGS}"
elif [ -f "../${DB_SETTINGS}" ] ; then
    . "../${DB_SETTINGS}"
else
    echo
    echo "Could not locate the dbsettings.props file."
    echo "You most likely need to build Scarab first."
    echo
    exit 1
fi

while [ "$#" -gt 0 ]
do
    case "$1" in
    --help|-\?|-h)
        usage=t
        break
        ;;
    --password|-p)
        password=t
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

if [ "${usage}" ] ; then
    echo
    echo "$CMDNAME upgrades the database."
    echo
    echo "Currently only works with MySQL and requres that"
    echo "Scarab already be built and configured to connect"
    echo "to the database properly."
    echo
    echo
    echo "This MySQL specific script does the following:"
    echo 
    echo "   1) Gets a list of template issue types (i.e., issue types
                where the parent id is not 0)."
    echo "   2) Deletes the entries for these issue types from the
                SCARAB_R_MODULE_ISSUE_TYPE table, as these mappings 
                are unnecessary."
    echo
    echo
    echo "Usage:"
        echo "  $CMDNAME [options]"
        echo
    echo "Example:"
        echo "  $CMDNAME -p"
        echo        
    echo "Options:"
    echo "  -p, --password             Prompt for password"
    echo "  -?, --help                 Usage"
    echo
    exit 0
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

    $ECHO_N "Enter password for \"${DB_USER}\": "$ECHO_C
    stty -echo >/dev/null 2>&1
    read FirstPw
    stty echo >/dev/null 2>&1
    DB_PASS="$FirstPw"
    echo
fi

MYSQL=`which mysql`
if [ ! -x "${MYSQL}" ] ; then
    echo
    echo "The MySQL binary needs to be in your PATH!"
    echo
    exit 1
fi

USERCMD=
if [ "${DB_USER}" != "" ] ; then
    USERCMD="--user=${DB_USER}"
fi

PASSCMD=
if [ ! -z "${password}" ] ; then
    PASSCMD="--password=${DB_PASS}"
fi
PORTCMD=
if [ "${DB_PORT}" != "" ] ; then
    PORTCMD="--port=${DB_PORT}"
fi
HOSTCMD="--host=${DB_HOST}"

MYSQLCMD="${HOSTCMD} ${PORTCMD} ${USERCMD} ${PASSCMD}"

## for remove module-issuetype mappings for template types
SQL="select SCARAB_ISSUE_TYPE.ISSUE_TYPE_ID from SCARAB_ISSUE_TYPE where parent_id>0"
echo $SQL | ${MYSQL} ${MYSQLCMD} ${DB_NAME} | grep -v '^ISSUE_TYPE_ID' | \
while read issuetypeid ; do
 echo "issuetypeid is: $issuetypeid"
 SQL="delete from SCARAB_R_MODULE_ISSUE_TYPE where ISSUE_TYPE_ID='$issuetypeid'"
 echo $SQL | ${MYSQL} ${MYSQLCMD} ${DB_NAME} 
done

