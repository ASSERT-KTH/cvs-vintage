#!/bin/sh

BUILD_DIR=`pwd`
cd ../src/sql
create-mysql-database.sh scarab scarab scarab-test
cd ${BUILD_DIR}
ant -buildfile run-tests.xml $@
