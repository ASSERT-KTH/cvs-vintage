#! /bin/ksh
#
# $Id: runtest.sh,v 1.5 1999/12/08 16:36:28 harishp Exp $

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

if [ -z "$JAVA_HOME" ]
then
JAVACMD=`which java`
if [ -z "$JAVACMD" ]
then
echo "Cannot find JAVA. Please set your PATH."
exit 1
fi
JAVA_BINDIR=`dirname $JAVACMD`
JAVA_HOME=$JAVA_BINDIR/..
fi

JAVACMD=$JAVA_HOME/bin/java

# Add tomcat-related classes
TOMCAT_HOME=..
. ${TOMCAT_HOME}/env.tomcat

CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

#$JAVACMD org.apache.tomcat.shell.Startup "$@" &
$TOMCAT_HOME/tomcat.sh start &
sleep 15
$JAVACMD -Dtest.hostname=$host -Dtest.port=$port org.apache.tools.moo.Main -testfile $test -debug
$TOMCAT_HOME/tomcat.sh stop
#$JAVACMD org.apache.tomcat.shell.Shutdown "$@"

if [ "$cp" != "" ]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
