#! /bin/ksh
#
# $Id: runtest.sh,v 1.8 2000/01/11 16:04:29 rubys Exp $

# Shell script to run test harness

if [ "$1" = "" ] ; then
   port=8080
else 
   port=$1
fi

host=localhost
test=testlist.txt

cp=$CLASSPATH

CLASSPATH=classes:lib/ant.jar:lib/moo.jar

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

java org.apache.tools.ant.Main -f test.xml $@

if [ "$cp" != "" ]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
