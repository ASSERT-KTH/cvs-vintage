#!/bin/sh
#
# $Id: tomcat.sh,v 1.23 2001/04/22 15:52:11 costin Exp $

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

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
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

if [ "$oldCP" != "" ]; then
    CLASSPATH=${CLASSPATH}:${oldCP}
fi

export CLASSPATH

## -------------------- Process options -------------------- 

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

  #Old code for -security: -Djava.security.manager -Djava.security.policy==${TOMCAT_HOME}/conf/tomcat.policy 
  # not needed, java starter will do that automatically

  if [ -f ${TOMCAT_HOME}/conf/ajp12.id ] ;  then  
        rm -f  ${TOMCAT_HOME}/conf/ajp12.id
  fi

  WAIT=0
  if [ "$1" = "-wait" ] ; then
    shift
    # wait at least 2 min
    WAIT=120
  fi

  if [ "$1" = "-noout" ] ; then
    shift
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME}  $MAIN "$@" >${TOMCAT_HOME}/logs/stdout.log 2>&1 &
  else
    echo Using classpath: ${CLASSPATH}
    echo Using JAVA_HOME: ${JAVA_HOME}
    echo Using TOMCAT_HOME: ${TOMCAT_HOME}
    $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME}  $MAIN "$@" &
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
  CLASSPATH=${CLASSPATH}:${TOMCAT_HOME}/lib/stop-tomcat.jar
  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} org.apache.tomcat.startup.StopTomcat "$@"

  if [ "$1" = "-force" ] ; then
    shift
    kill -9 `cat $TOMCAT_HOME/logs/tomcat.pid`
  fi

elif [ "$1" = "run" ] ; then 
  shift 
  echo Using classpath: ${CLASSPATH}
  echo Using JAVA_HOME: ${JAVA_HOME}
  echo Using TOMCAT_HOME: ${TOMCAT_HOME}
  $JAVACMD $TOMCAT_OPTS -Dtomcat.home=${TOMCAT_HOME} $MAIN "$@" 

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
  echo "tomcat (start|env|run|stop|ant)"
  echo "        start - start tomcat in the background"
  echo "        run   - start tomcat in the foreground"
  echo "        run -wait - wait until tomcat is initialized before returning  "
  echo "            -security - use a SecurityManager when starting"
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
