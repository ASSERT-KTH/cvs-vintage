#!/bin/sh
#
# $Id: tomcat.sh,v 1.31 2001/10/21 17:33:41 larryi Exp $

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
if [ "$TOMCAT_INSTALL" = "" ] ; then
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
  
  TOMCAT_INSTALL_1=`dirname "$PRG"`/..
  if [ "$DEBUG_HOMEFIND" != "false" ] ; then
    echo "Guessing TOMCAT_INSTALL from tomcat.sh to ${TOMCAT_INSTALL_1}" 
  fi
    if [ -d ${TOMCAT_INSTALL_1}/lib ] ; then 
        TOMCAT_INSTALL=${TOMCAT_INSTALL_1}
        if [ "$DEBUG_HOMEFIND" != "false" ] ; then
          echo "Setting TOMCAT_INSTALL to $TOMCAT_INSTALL"
        fi
    fi
fi


if [ "$TOMCAT_INSTALL" = "" ] ; then
  # try to find tomcat
  if [ -d ${HOME}/opt/tomcat/conf ] ; then 
    TOMCAT_INSTALL=${HOME}/opt/tomcat
    if [ "$DEBUG_HOMEFIND" != "false" ] ; then
      echo "Defaulting TOMCAT_INSTALL to $TOMCAT_INSTALL"
    fi
  fi

  if [ -d /opt/tomcat/conf ] ; then 
    TOMCAT_INSTALL=/opt/tomcat
    if [ "$DEBUG_HOMEFIND" != "false" ] ; then
      echo "Defaulting TOMCAT_INSTALL to $TOMCAT_INSTALL"
    fi
  fi
 
  # Add other "standard" locations for tomcat
fi

if [ "$TOMCAT_INSTALL" = "" ] ; then
    echo TOMCAT_INSTALL not set, you need to set it or install in a standard location
    exit 1
fi

if [ "$TOMCAT_OPTS" = "" ] ; then
  TOMCAT_OPTS=""
fi

if [ "$JSPC_OPTS" = "" ] ; then
  JSPC_OPTS=""
fi

if [ "$TOMCAT_HOME" = "" ] ; then
    if [ -d ./conf ] ; then 
        TOMCAT_HOME=.
    elif [ -d ../conf ] ; then 
        TOMCAT_HOME=..
    else
        TOMCAT_HOME=$TOMCAT_INSTALL
    fi
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
CLASSPATH=${TOMCAT_INSTALL}/lib/tomcat.jar

# Ignore previous CLASSPATH

# This is consistent with "java -jar tomcat.jar "
export CLASSPATH

## -------------------- Process options -------------------- 
# add tomcat.policy - even if we don't use sandbox, it doesn't hurt
TOMCAT_OPTS="$TOMCAT_OPTS -Djava.security.policy==${TOMCAT_HOME}/conf/tomcat.policy "


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

  MAX_WAIT=360
  WAIT=0
  if [ "$1" = "-wait" ] ; then
    shift
    # wait at least 6 min 
    WAIT=${MAX_WAIT}
  fi
    
  if [ "$1" = "-noout" ] ; then
    shift
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME}  $MAIN start $@ >${TOMCAT_HOME}/logs/stdout.log 2>&1 &
  else
    echo Using classpath: ${CLASSPATH}
    echo Using JAVA_HOME: ${JAVA_HOME}
    echo Using TOMCAT_HOME: ${TOMCAT_HOME}
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME}  $MAIN start $@ &
  fi


  JAVA_PID=$!
  echo $JAVA_PID > ${TOMCAT_HOME}/logs/tomcat.pid

  # Wait for ajp12.id signaling end of startup
  if [ ! "$WAIT" = "0" ] ; then 
    while [ ! -f ${TOMCAT_HOME}/conf/ajp12.id ] ; do 
        sleep 1

        WAIT=`expr $WAIT - 1`
        if [ "$WAIT" = "0" ] ; then
            echo "Tomcat was not ready after ${MAX_WAIT} seconds, giving up waiting "
            break;
        fi
    done
  fi

elif [ "$1" = "stop" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  echo Using JAVA_HOME: ${JAVA_HOME}
  echo Using TOMCAT_HOME: ${TOMCAT_HOME}
  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN stop $@

  if [ "$1" = "-force" ] ; then
    shift
    echo "Killing: `cat $TOMCAT_HOME/logs/tomcat.pid`"
    kill -9 `cat $TOMCAT_HOME/logs/tomcat.pid`
  fi

elif [ "$1" = "run" ] ; then 
  shift 
  # Backward compat
  if [ "$1" = "enableAdmin" ] ; then
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN enableAdmin $@ 
  elif  [ "$1" = "-enableAdmin" ] ; then  
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN enableAdmin $@ 
  else
    echo Using classpath: ${CLASSPATH}
    echo Using JAVA_HOME: ${JAVA_HOME}
    echo Using TOMCAT_HOME: ${TOMCAT_HOME}
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN start $@ 
  fi
elif [ "$1" = "enableAdmin" ] ; then 

  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN enableAdmin $@

elif [ "$1" = "estart" ] ; then 

  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN estart $@

elif [ "$1" = "jspc" ] ; then 
    shift 
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN jspc $@

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
    (cd $TOMCAT_HOME; $JAVACMD $JSPC_OPTS -Dtomcat.home=${TOMCAT_HOME} org.apache.jasper.JspC $@ )

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
  echo "$0 (start|run|stop|enableAdmin|estart|env|jspc)"
  echo "  start            - start tomcat in the background"
  echo "  start -security  -   use a SecurityManager when starting"
  echo "  start -noout     -   redirect stdout/stderr to \$TOMCAT_HOME/logs/stdout.log"
  echo "  start -wait      -   wait until tomcat is initialized before returning"
  echo "  start -help      -   more options"
  echo "                         (config, debug, estart, home, install, jkconf, sandbox)"
  echo "  run              - start tomcat in the foreground"
  echo "  run -security    -   use a SecurityManager when starting"
  echo "  stop             - stop tomcat"
  echo "  stop -force      -   stop tomcat with the 'kill' command if necessary"
  echo "  stop -help       -   more options"
  echo "                         (ajpid, host, home, pass, port)"
  echo "  enableAdmin      - Trust the admin web application,"
  echo "                     i.e. rewrites conf/apps-admin.xml with trusted=\"true\""
  echo "  estart           - Start Tomcat using the/your EmbededTomcat class which"
  echo "                     uses a hardcoded set of modules"
  echo "  env              - set CLASSPATH and TOMCAT_HOME env. variables"
  echo "  jspc             - run jsp pre compiler"
  exit 0
fi


if [ "$oldCP" != "" ]; then
    CLASSPATH=${oldCP}
    export CLASSPATH
else
    unset CLASSPATH
fi
