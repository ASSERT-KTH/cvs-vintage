#!/bin/sh
#
# $Id: tomcat.sh,v 1.5 1999/12/15 22:53:25 costin Exp $

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

if [ -z "$JAVA_HOME" ] ;  then
  JAVACMD=`which java`
  if [ -z "$JAVACMD" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BINDIR=`dirname $JAVACMD`
  JAVA_HOME=$JAVA_BINDIR/..
fi

if [ "$JAVACMD" = "" ] ; then 
   # it may be defined in env - including flags!!
   JAVACMD=$JAVA_HOME/bin/java
fi


oldCP=$CLASSPATH
 
CLASSPATH=${TOMCAT_HOME}/webserver.jar
CLASSPATH=${CLASSPATH}:${TOMCAT_HOME}/lib/servlet.jar
CLASSPATH=${CLASSPATH}:${TOMCAT_HOME}/lib/jasper.jar
CLASSPATH=${CLASSPATH}:${TOMCAT_HOME}/lib/xml.jar
## CLASSPATH=${CLASSPATH}:${TOMCAT_HOME}/webpages/WEB-INF/classes/jsp/beans

CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar


# Backdoor classpath setting for development purposes when all classes
# are compiled into a /classes dir and are not yet jarred.
if [ -d ${TOMCAT_HOME}/classes ]; then
    CLASSPATH=${TOMCAT_HOME}/classes:${CLASSPATH}
fi

if [ "$oldCP" != "" ]; then
    CLASSPATH=${CLASSPATH}:${oldCP}
fi

export CLASSPATH

if [ ! -f server.xml ] ; then 
   if [ "$2" = "" ] ; then
     # Probably we are in a wrong directory, use tomcat_home
     # If arguments are passed besides start/stop, probably a -f was used,
     # or the user knows what he's doing
     echo cd ${TOMCAT_HOME} 
     cd ${TOMCAT_HOME}
   fi
fi

# We start the server up in the background for a couple of reasons:
#   1) It frees up your command window
#   2) You should use `stop` option instead of ^C to bring down the server
if [ "$1" = "start" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  $JAVACMD org.apache.tomcat.shell.Startup "$@" &

elif [ "$1" = "stop" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  $JAVACMD org.apache.tomcat.shell.Shutdown "$@"

elif [ "$1" = "run" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  $JAVACMD org.apache.tomcat.shell.Startup "$@" 
  # no &

## Call it with source tomcat.sh to set the env for tomcat
elif [ "$1" = "env" ] ; then 
  shift 
  echo Setting classpath to: ${CLASSPATH}
  oldCP=$CLASSPATH

else
  echo "Usage:"
  echo "tomcat [start|stop]"
  exit 0
fi


if [ "$oldCP" != "" ]; then
    CLASSPATH=${oldCP}
    export CLASSPATH
else
    unset CLASSPATH
fi
