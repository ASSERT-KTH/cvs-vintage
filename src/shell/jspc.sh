#!/bin/sh
#
# $Id: jspc.sh,v 1.2 2000/02/09 06:50:50 shemnon Exp $

# Shell script to runt JspC

# There are other, simpler commands to run JspC.   The two
# commented commands good replacements. The first works well with
# Java Platform 1.1 based runtimes. The second works well with
# Java2 Platform based runtimes.

#jre -cp runner.jar:servlet.jar:classes org.apache.jasper.JspC $*
#java -cp runner.jar:servlet.jar:classes org.apache.jasper.JspC $*

if [ -f $HOME/.tomcatrc ] ; then 
  . $HOME/.tomcatrc
fi

if [ "$TOMCAT_HOME" = "" ] ; then
  # try to find tomcat
  if [ -d ${HOME}/opt/tomcat/conf ] ; then 
    TOMCAT_HOME=${HOME}/opt/tomcat
  fi

  if [ -d /opt/tomcat/conf ] ; then 
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
  
  TOMCAT_HOME=`dirname "$PRG"`/..

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
 
CLASSPATH=.
for i in ${TOMCAT_HOME}/lib/* ; do
  CLASSPATH=${CLASSPATH}:$i
done

CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
#echo XXX $CLASSPATH


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

$JAVACMD org.apache.jasper.JspC "$@"

if [ "$oldCP" != "" ]; then
    CLASSPATH=${oldCP}
    export CLASSPATH
else
    unset CLASSPATH
fi
