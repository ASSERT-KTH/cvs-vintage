#! /bin/sh

# $Id: build.sh,v 1.11 2000/05/01 15:46:47 craigmcc Exp $

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

cp=../jakarta-ant/lib/ant.jar:../jakarta-servletapi/lib/servlet.jar:../jakarta-tools/moo.jar:../build/tomcat/classes:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dev.jar

$JAVACMD -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"

