#!/bin/sh
case "${1}" in
    -h|--help|help)
    echo "Usage: scarab.sh [-l] [-h]"
    echo "-l    Default JVM memory allocation"
    echo "-h    This usage information"
    exit
    ;;
esac

if [ "${CLASSPATH}" != "" ] ; then
    echo "Classpath: ${CLASSPATH}"
fi

if [ "${1}" != "-l" ] ; then
    CATALINA_OPTS="-Xms96M -Xmx256M"
    export CATALINA_OPTS
fi

WEBAPP_DIR="`dirname ${0} 2> /dev/null`"
if [ -z "${WEBAPP_DIR}" -o ! -d "${WEBAPP_DIR}" ]; then
    WEBAPP_DIR='.'
fi

"${WEBAPP_DIR}/bin/catalina.sh" run
