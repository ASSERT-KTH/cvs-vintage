#! /bin/sh
#
# $Id: runtest.sh,v 1.1 1999/10/15 21:37:45 costin Exp $

# Shell script to run test harness
 
host=localhost
port=8080
test=testlist.txt

baseDir=../../..
tomcatBuildDir=$baseDir/build/tomcat
toolsDir=$baseDir/jakarta-tools

jsdkJars=${tomcatBuildDir}/webserver.jar:${tomcatBuildDir}/lib/servlet.jar
jspJars=${tomcatBuildDir}/lib/jasper.jar
beanJars=
miscJars=${toolsDir}/projectx-tr2.jar:${toolsDir}/moo.jar
appJars=${jsdkJars}:${jspJars}:${miscJars}:${tomcatBuildDir}/classes
sysJars=${JAVA_HOME}/lib/tools.jar

appClassPath=./classes:${appJars}
cp=$CLASSPATH

CLASSPATH=${appClassPath}:${sysJars}
export CLASSPATH

if [ "$cp" != "" ]; then
    CLASSPATH=${CLASSPATH}:${cp}
    export CLASSPATH
fi

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
