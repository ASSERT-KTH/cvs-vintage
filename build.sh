#! /bin/sh

# $Id: build.sh,v 1.4 1999/12/03 17:01:15 harishp Exp $

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

cp=../jakarta-tools/ant.jar:../jakarta-tools/projectx-tr2.jar:../build/tomcat/classes:$JAVA_HOME/lib/tools.jar

$JAVACMD -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"

chmod +x `find ../build -name "*.sh"`
