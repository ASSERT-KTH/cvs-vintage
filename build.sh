#! /bin/sh

# $Id: build.sh,v 1.10 2000/04/25 16:56:24 craigmcc Exp $

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

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

JAVACMD=$JAVA_HOME/bin/java $ANT_OPTS

cp=../jakarta-ant/lib/ant.jar:../jakarta-tools/moo.jar:../jakarta-ant/lib/xml.jar:../build/tomcat/classes:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dev.jar

$JAVACMD -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"


