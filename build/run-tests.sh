#!/bin/sh

pushd ../src/sql
create-mysql-database.sh scarab scarab scarab-test
popd
ant -buildfile run-tests.xml $@
