#!/bin/sh

if [ "${1}" = "-h" ] ; then
    echo "Usage: scarab.sh [-l] [-h]"
    echo "-l    Default JVM memory allocation"
    echo "-h    This usage information"
    exit
fi

if [ "${1}" != "-l" ] ; then
    CATALINA_OPTS="-Xms96M -Xmx256M"
    export CATALINA_OPTS
fi

./bin/catalina.sh run
