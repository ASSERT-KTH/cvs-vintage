#!/bin/sh
#
# $Id: tomcat.sh,v 1.4 1999/12/15 00:30:23 costin Exp $

# Shell script to start and stop the server

# There are other, simpler commands to startup the runner. The two
# commented commands good replacements. The first works well with
# Java Platform 1.1 based runtimes. The second works well with
# Java2 Platform based runtimes.

#jre -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Startup $*
#java -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Startup $*

if [ -f $HOME/.tomcatrc ] ; then 
  . $HOME/.tomcatrc
fi

if [ "$TOMCAT_HOME" = "" ] ; then
  # try to find tomcat
  if [ -d ${HOME}/opt/tomcat ] ; then 
    TOMCAT_HOME=${HOME}/opt/tomcat
  fi

  if [ -d /opt/tomcat ] ; then 
    TOMCAT_HOME=/opt/tomcat
  fi

  ## resolve links - $0 may be a link to ant's home
  PRG=$0
  progname=`basename $0`
  
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG="`dirname $PRG`/$link"
    fi
  done
  
  TOMCAT_HOME=`dirname "$PRG"`

fi

baseDir=`dirname $0`

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

if [ ! -f server.xml ] ; then 
   # Probably we are in a wrong directory, use tomcat_home
   cd ${TOMCAT_HOME}
fi

# We start the server up in the background for a couple of reasons:
#   1) It frees up your command window
#   2) You should use `stop` option instead of ^C to bring down the server

if test "$1" = "start" 
then 
shift 
echo Using classpath: ${CLASSPATH}
$JAVACMD org.apache.tomcat.shell.Startup "$@" &
elif test "$1" = "stop"
then 
shift 
echo Using classpath: ${CLASSPATH}
$JAVACMD org.apache.tomcat.shell.Shutdown "$@"
else
echo "Usage:"
echo "tomcat [start|stop]"
exit 0
fi


if [ "$cp" != "" ]; then
    CLASSPATH=${cp}
    export CLASSPATH
else
    unset CLASSPATH
fi
