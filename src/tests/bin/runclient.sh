#!/bin/sh

# Shell script to run test harness
port=8080
host=localhost
test=testlist.txt
JAVACMD=java

# override settings
if [ -f ${HOME}/.moorc ] ; then
  . ${HOME}/.moorc
fi

CLASSPATH=classes:lib/moo.jar:${CLASSPATH}
export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

$JAVACMD -Dtest.hostname=$host -Dtest.port=$port org.apache.tools.moo.Main -testfile $test -debug
