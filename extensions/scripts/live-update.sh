#!/bin/sh

LIVEDIR=/Users/jon/live/scarab

if [ ! -f "${LIVEDIR}/.canupdate" ] ; then
    echo "Need to create a ${LIVEDIR}/.canupdate file before "
    echo "you can run this script. You may also need to edit "
    echo "this script and define the LIVEDIR variable."
    exit
fi

CURDIR=`pwd`

cd "${LIVEDIR}/target"

./bin/shutdown.sh

cd "webapps/scarab/WEB-INF"

rm -rf conf classes intake-xml.ser lib sql src web.xml 

cd "${LIVEDIR}"

cvs up

cd "${LIVEDIR}/build"

ant

cd "${LIVEDIR}/target"

./bin/startup.sh

cd ${CURDIR}
