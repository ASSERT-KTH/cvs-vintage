#!/bin/sh
#
# $Id: tomcat.sh,v 1.26 2001/08/21 05:55:50 costin Exp $

# Shell script to start and stop the server

# There are other, simpler commands to startup the runner. The two
# commented commands good replacements. The first works well with
# Java Platform 1.1 based runtimes. The second works well with
# Java2 Platform based runtimes.
#jre -cp lib/tomcat.jar org.apache.tomcat.startup.Main $*
#java -cp lib/tomcat.jar org.apache.tomcat.startup.Main $*
#java -jar lib/tomcat.jar


# Read local properties 
if [ -f $HOME/.tomcatrc ] ; then 
  . $HOME/.tomcatrc
fi

# -------------------- Guess TOMCAT_HOME --------------------
DEBUG_HOMEFIND=false
# Follow symbolic links to the real tomcat.sh
# Extract the base dir.
# Look in well-known places if this fails
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
  if [ "$DEBUG_HOMEFIND" != "false" ] ; then
    echo "Guessing TOMCAT_HOME from tomcat.sh to ${TOMCAT_HOME_1}" 
  fi
    if [ -d ${TOMCAT_HOME_1}/conf ] ; then 
	TOMCAT_HOME=${TOMCAT_HOME_1}
        if [ "$DEBUG_HOMEFIND" != "false" ] ; then
          echo "Setting TOMCAT_HOME to $TOMCAT_HOME"
	fi
    fi
fi


if [ "$TOMCAT_HOME" = "" ] ; then
  # try to find tomcat
  if [ -d ${HOME}/opt/tomcat/conf ] ; then 
    TOMCAT_HOME=${HOME}/opt/tomcat
    if [ "$DEBUG_HOMEFIND" != "false" ] ; then
      echo "Defaulting TOMCAT_HOME to $TOMCAT_HOME"
    fi
  fi

  if [ -d /opt/tomcat/conf ] ; then 
    TOMCAT_HOME=/opt/tomcat
    if [ "$DEBUG_HOMEFIND" != "false" ] ; then
      echo "Defaulting TOMCAT_HOME to $TOMCAT_HOME"
    fi
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

if [ "$JSPC_OPTS" = "" ] ; then
  JSPC_OPTS=""
fi

## -------------------- Find JAVA_HOME --------------------

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

## -------------------- Prepare CLASSPATH --------------------
MAIN=org.apache.tomcat.startup.Main
export MAIN

oldCP=$CLASSPATH
unset CLASSPATH
CLASSPATH=${TOMCAT_HOME}/lib/tomcat.jar

# Ignore previous CLASSPATH
#if [ "$oldCP" != "" ]; then
#    CLASSPATH=${CLASSPATH}:${oldCP}
#fi

if [ -f ${JAVA_HOME}/jre/lib/rt.jar ] ; then
    CLASSPATH=${CLASSPATH}:${JAVA_HOME}/jre/lib/rt.jar
fi

# This is consistent with "java -jar tomcat.jar "
export CLASSPATH

## -------------------- Process options -------------------- 
# add tomcat.policy - even if we don't use sandbox, it doesn't hurt
TOMCAT_OPTS="$TOMCAT_OPTS -Djava.security.policy==${TOMCAT_HOME}/lib/tomcat.policy "


# We start the server up in the background for a couple of reasons:
#   1) It frees up your command window
#   2) You should use `stop` option instead of ^C to bring down the server
if [ "$1" = "start_msg" ]; then
  shift
  echo "Starting Tomcat Servlet Engine"

elif [ "$1" = "stop_msg" ]; then
  shift
  echo "Stopping Tomcat Servlet Engine"

elif [ "$1" = "start" ] ; then 
  shift 

  if [ -f ${TOMCAT_HOME}/conf/ajp12.id ] ;  then  
        rm -f  ${TOMCAT_HOME}/conf/ajp12.id
  fi

  WAIT=0
  if [ "$1" = "-wait" ] ; then
    shift
    # wait at least 6 min 
    WAIT=360
  fi
    
  if [ "$1" = "-noout" ] ; then
    shift
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME}  $MAIN start "$@" >${TOMCAT_HOME}/logs/stdout.log 2>&1 &
  else
    echo Using classpath: ${CLASSPATH}
    echo Using JAVA_HOME: ${JAVA_HOME}
    echo Using TOMCAT_HOME: ${TOMCAT_HOME}
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME}  $MAIN start "$@" &
  fi


  JAVA_PID=$!
  echo $JAVA_PID > ${TOMCAT_HOME}/logs/tomcat.pid

  # Wait for ajp12.id signaling end of startup
  if [ ! "$WAIT" = "0" ] ; then 
    while [ ! -f ${TOMCAT_HOME}/conf/ajp12.id ] ; do 
        sleep 1

        WAIT=`expr $WAIT - 1`
        if [ "$WAIT" = "0" ] ; then
            echo "Tomcat was no ready after 120 seconds, giving up waiting "
	    break;
        fi
    done
  fi

elif [ "$1" = "stop" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  echo Using JAVA_HOME: ${JAVA_HOME}
  echo Using TOMCAT_HOME: ${TOMCAT_HOME}
  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN stop "$@"

  if [ "$1" = "-force" ] ; then
    shift
    echo "Killing: `cat $TOMCAT_HOME/logs/tomcat.pid`"
    kill -9 `cat $TOMCAT_HOME/logs/tomcat.pid`
  fi

elif [ "$1" = "run" ] ; then 
  shift 
  # Backward compat
  if [ "$1" = "enableAdmin" ] ; then
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN enableAdmin "$@" 
  elif  [ "$1" = "-enableAdmin" ] ; then  
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN enableAdmin "$@" 
  else
    echo Using classpath: ${CLASSPATH}
    echo Using JAVA_HOME: ${JAVA_HOME}
    echo Using TOMCAT_HOME: ${TOMCAT_HOME}
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN start "$@" 
  fi
elif [ "$1" = "enableAdmin" ] ; then 

  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN enableAdmin "$@"

elif [ "$1" = "estart" ] ; then 

  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN estart "$@"

elif [ "$1" = "jspc" ] ; then 
    shift 
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN jspc "$@"

elif [ "$1" = "jspcOrig" ] ; then 
    shift 
    CLASSPATH=.
    for i in ${TOMCAT_HOME}/lib/container/* ${TOMCAT_HOME}/lib/common/* ; do
	CLASSPATH=${CLASSPATH}:$i
    done
    CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
    # Backdoor classpath setting for development purposes when all classes
	# are compiled into a /classes dir and are not yet jarred.
    if [ -d ${TOMCAT_HOME}/classes ]; then
	    CLASSPATH=${TOMCAT_HOME}/classes:${CLASSPATH}
    fi
    
    if [ "$oldCP" != "" ]; then
	CLASSPATH=${CLASSPATH}:${oldCP}
    fi
    (cd $TOMCAT_HOME; $JAVACMD $JSPC_OPTS -Dtomcat.home=${TOMCAT_HOME} org.apache.jasper.JspC "$@" )

elif [ "$1" = "env" ] ; then 
  ## Call it with source tomcat.sh to set the env for tomcat
  shift 
  echo Setting classpath to: ${CLASSPATH}
  # -------------------- Add all classes in common, container, apps - 
  # Used if you want to do command-line javac, etc
  ## Temp - old script 
  for i in ${TOMCAT_HOME}/lib/* ${TOMCAT_HOME}/lib/common/* ${TOMCAT_HOME}/lib/container/* ${TOMCAT_HOME}/lib/apps/* ; do
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
  oldCP=$CLASSPATH

else
  echo "Usage:"
  echo "tomcat (start|env|run|stop|jspc)"
  echo "        start - start tomcat in the background"
  echo "        run   - start tomcat in the foreground"
  echo "        run -wait - wait until tomcat is initialized before returning  "
  echo "            -security - use a SecurityManager when starting"
  echo "        stop  - stop tomcat"
  echo "        env  -  set CLASSPATH and TOMCAT_HOME env. variables"
  echo "        jspc - run jsp pre compiler"

  exit 0
fi


if [ "$oldCP" != "" ]; then
    CLASSPATH=${oldCP}
    export CLASSPATH
else
    unset CLASSPATH
fi
