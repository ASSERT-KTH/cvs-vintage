#! /bin/sh

# $Id: build.sh,v 1.9 2000/03/20 23:47:12 jhunter Exp $

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

cp=../jakarta-ant/lib/ant.jar:../jakarta-tools/moo.jar:../jakarta-ant/lib/xml.jar:../build/tomcat/classes:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dev.jar

$JAVACMD -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"
