#! /bin/sh
#
# $Id: runtest.sh,v 1.1 1999/10/09 00:20:56 duncan Exp $

# Shell script to run test harness
 
host=localhost
port=8080
test=testlist.txt

baseDir=../../dist/tomcat

jsdkJars=${baseDir}/webserver.jar:${baseDir}/lib/servlet.jar
jspJars=${baseDir}/lib/jasper.jar
beanJars=
miscJars=${baseDir}/lib/xml.jar:./lib/moo.jar
appJars=${jsdkJars}:${jspJars}:${miscJars}
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

java org.apache.tomcat.shell.Startup $* &
sleep 5
java -Dtest.hostname=$host -Dtest.port=$port com.sun.moo.Main \
    -testfile $test -debug
java org.apache.tomcat.shell.Shutdown $*

if [ "$cp" != "" ]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
