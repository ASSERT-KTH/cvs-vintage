#! /bin/sh

# $Id: build.sh,v 1.7 2000/01/15 17:35:10 rubys Exp $

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

cp=../jakarta-ant/lib/ant.jar:../jakarta-tools/moo.jar:../jakarta-ant/lib/xml.jar:../build/tomcat/classes:$JAVA_HOME/lib/tools.jar

$JAVACMD -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"

chmod +x `find ../build -name "*.sh"`
