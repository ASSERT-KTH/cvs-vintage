#!/bin/sh
#
# $Id: tomcat.sh,v 1.20 2001/02/06 04:44:53 costin Exp $

# Shell script to start and stop the server

# There are other, simpler commands to startup the runner. The two
# commented commands good replacements. The first works well with
# Java Platform 1.1 based runtimes. The second works well with
# Java2 Platform based runtimes.

#jre -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Startup $*
#java -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Startup $*

NEW_LOADER=1
export NEW_LOADER

if [ -f $HOME/.tomcatrc ] ; then 
  . $HOME/.tomcatrc
fi

if [ "$TOMCAT_HOME" = "" ] ; then
  ## resolve links - $0 may be a link to  home
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
  
  TOMCAT_HOME_1=`dirname "$PRG"`/..
  echo "Guessing TOMCAT_HOME from tomcat.sh to ${TOMCAT_HOME_1}" 
    if [ -d ${TOMCAT_HOME_1}/conf ] ; then 
	TOMCAT_HOME=${TOMCAT_HOME_1}
	echo "Setting TOMCAT_HOME to $TOMCAT_HOME"
    fi
fi


if [ "$TOMCAT_HOME" = "" ] ; then
  # try to find tomcat
  if [ -d ${HOME}/opt/tomcat/conf ] ; then 
    TOMCAT_HOME=${HOME}/opt/tomcat
    echo "Defaulting TOMCAT_HOME to $TOMCAT_HOME"
  fi

  if [ -d /opt/tomcat/conf ] ; then 
    TOMCAT_HOME=/opt/tomcat
    echo "Defaulting TOMCAT_HOME to $TOMCAT_HOME"
  fi
 
  # Add other "standard" locations for tomcat
fi

if [ "$TOMCAT_HOME" = "" ] ; then
    echo TOMCAT_HOME not set, you need to set it or install in a standard location
    exit 1
fi

if [ "$TOMCAT_OPTS" = "" ] ; then
  TOMCAT_OPTS=""
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

if [ "$JSPC_OPTS" = "" ] ; then
  JSPC_OPTS=""
fi

if [ -z "$JAVA_HOME" ] ;  then
  JAVA=`which java`
  if [ -z "$JAVA" ] ; then
    echo "Cannot find JAVA. Please set your PATH."
    exit 1
  fi
  JAVA_BINDIR=`dirname $JAVA`
  JAVA_HOME=$JAVA_BINDIR/..
fi

if [ "$JAVACMD" = "" ] ; then 
   # it may be defined in env - including flags!!
   JAVACMD=$JAVA_HOME/bin/java
fi


oldCP=$CLASSPATH
 
unset CLASSPATH

if [ "$NEW_LOADER" = "1" ]; then
  MAIN=org.apache.tomcat.startup.Main
  export MAIN
  CLASSPATH=${TOMCAT_HOME}/lib/tomcat.jar
else
  MAIN=org.apache.tomcat.startup.Tomcat

## Temp - old script 
for i in ${TOMCAT_HOME}/lib/* ; do
  if [ "$CLASSPATH" != "" ]; then
    CLASSPATH=${CLASSPATH}:$i
  else
    CLASSPATH=$i
  fi
done

if [ -f ${JAVA_HOME}/lib/tools.jar ] ; then
   # We are probably in a JDK1.2 environment
   CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
fi

# Backdoor classpath setting for development purposes when all classes
# are compiled into a /classes dir and are not yet jarred.
if [ -d ${TOMCAT_HOME}/classes ]; then
    CLASSPATH=${TOMCAT_HOME}/classes:${CLASSPATH}
fi

if [ "$oldCP" != "" ]; then
    CLASSPATH=${CLASSPATH}:${oldCP}
fi

# End - NEW_LOADER
fi

export CLASSPATH

# We start the server up in the background for a couple of reasons:
#   1) It frees up your command window
#   2) You should use `stop` option instead of ^C to bring down the server
if [ "$1" = "start" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  echo Using JAVA_HOME: ${JAVA_HOME}
  echo Using TOMCAT_HOME: ${TOMCAT_HOME}
  if [ "$1" = "-security" ] ; then
    echo Starting with a SecurityManager
    $JAVACMD $TOMCAT_OPTS -Djava.security.manager -Djava.security.policy==${TOMCAT_HOME}/conf/tomcat.policy -Dtomcat.home=${TOMCAT_HOME}  $MAIN "$@" &
  else
  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME}  $MAIN "$@" &
  fi
#   $JAVACMD org.apache.tomcat.shell.Startup "$@" &

elif [ "$1" = "stop" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  echo Using JAVA_HOME: ${JAVA_HOME}
  echo Using TOMCAT_HOME: ${TOMCAT_HOME}
  CLASSPATH=${CLASSPATH}:${TOMCAT_HOME}/lib/stop-tomcat.jar
  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} org.apache.tomcat.startup.StopTomcat "$@"

elif [ "$1" = "run" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  echo Using JAVA_HOME: ${JAVA_HOME}
  echo Using TOMCAT_HOME: ${TOMCAT_HOME}
  if [ "$1" = "-security" ] ; then
    echo Starting with a SecurityManager
    $JAVACMD $TOMCAT_OPTS -Djava.security.manager -Djava.security.policy==${TOMCAT_HOME}/conf/tomcat.policy -Dtomcat.home=${TOMCAT_HOME} $MAIN "$@"
  else
  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN "$@" 
  fi
#  $JAVACMD org.apache.tomcat.shell.Startup "$@" 
  # no &

elif [ "$1" = "ant" ] ; then 
  shift 

  $JAVACMD $ANT_OPTS -Dant.home=${TOMCAT_HOME} -Dtomcat.home=${TOMCAT_HOME} org.apache.tools.ant.Main $@

elif [ "$1" = "jspc" ] ; then 
  shift 

  $JAVACMD $JSPC_OPTS -Dtomcat.home=${TOMCAT_HOME} org.apache.jasper.JspC "$@"

elif [ "$1" = "env" ] ; then 
  ## Call it with source tomcat.sh to set the env for tomcat
  shift 
  echo Setting classpath to: ${CLASSPATH}
  oldCP=$CLASSPATH

else
  echo "Usage:"
  echo "tomcat (start|env|run|stop|ant)"
  echo "        start - start tomcat in the background"
  echo "        run   - start tomcat in the foreground"
  echo "              -security - use a SecurityManager when starting"
  echo "        stop  - stop tomcat"
  echo "        env  -  set CLASSPATH and TOMCAT_HOME env. variables"
  echo "        ant  - run ant script in tomcat context ( classes, directories, etc)"
  echo "        jspc - run jsp pre compiler"

  exit 0
fi


if [ "$oldCP" != "" ]; then
    CLASSPATH=${oldCP}
    export CLASSPATH
else
    unset CLASSPATH
fi
