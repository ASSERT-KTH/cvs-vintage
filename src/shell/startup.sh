#! /bin/sh
#
# $Id: startup.sh,v 1.1 1999/10/09 00:20:56 duncan Exp $

# Shell script to startup the server

# There are other, simpler commands to startup the runner. The two
# commented commands good replacements. The first works well with
# Java Platform 1.1 based runtimes. The second works well with
# Java2 Platform based runtimes.

#jre -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Startup $*
#java -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Startup $*

baseDir=`dirname $0`

jsdkJars=${baseDir}/webserver.jar:${baseDir}/lib/servlet.jar
jspJars=${baseDir}/lib/jasper.jar
beanJars=${baseDir}/webpages/WEB-INF/classes/jsp/beans
miscJars=${baseDir}/lib/xml.jar
appJars=${jsdkJars}:${jspJars}:${beanJars}:${miscJars}
sysJars=${JAVA_HOME}/lib/tools.jar

appClassPath=${appJars}
cp=$CLASSPATH

# Backdoor classpath setting for development purposes when all classes
# are compiled into a /classes dir and are not yet jarred.

if [ -d ${baseDir}/classes ]; then
    appClassPath=${baseDir}/classes:${appClassPath}
fi

CLASSPATH=${appClassPath}:${sysJars}
export CLASSPATH

if [ "$cp" != "" ]; then
    CLASSPATH=${CLASSPATH}:${cp}
    export CLASSPATH
fi

echo Using classpath: ${CLASSPATH}

# We start the server up in the background for a couple of reasons:
#   1) It frees up your command window
#   2) You should use `stopserver` instead of ^C to bring down the server

java org.apache.tomcat.shell.Startup $* &

if [ "$cp" != "" ]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
