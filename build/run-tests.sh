#!/bin/sh

ONE=${1}

if [ "${ONE}" = "-h" ] ; then
    echo "Usage: run-test.sh [test-to-run]"
    echo "Optionally:"
    echo "       -c            <- Create test database"
    echo "       -c User Pass"
    echo "       -h            <- Usage"
    exit
fi

if [ "${ONE}" = "-c" ] ; then
    BUILD_DIR=`pwd`
    cd ../src/sql
    NAME=${2}
    PASS=${3}
    create-db.sh "${NAME}" "${PASS}" scarab-test
    cd ${BUILD_DIR}
    exit
fi

ant -buildfile run-tests.xml $@
