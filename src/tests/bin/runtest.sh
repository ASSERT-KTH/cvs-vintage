#! /bin/ksh
#
# $Id: runtest.sh,v 1.3 1999/11/02 00:46:46 costin Exp $

# Shell script to run test harness
 
host=localhost
port=8080
test=testlist.txt

cp=$CLASSPATH

CLASSPATH=classes:lib/moo.jar

if [ "$cp" != "" ]; then
    CLASSPATH=${CLASSPATH}:${cp}
fi

if [ "$1" != "" ]; then
    port=$1
fi

# Add tomcat-related classes
TOMCAT_HOME=..
. ${TOMCAT_HOME}/env.tomcat

export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

java org.apache.tomcat.shell.Startup "$@" &
sleep 5
java -Dtest.hostname=$host -Dtest.port=$port org.apache.tools.moo.Main \
    -testfile $test -debug
java org.apache.tomcat.shell.Shutdown "$@"

if [ "$cp" != "" ]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
