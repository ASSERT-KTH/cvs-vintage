#!/bin/sh
# -----------------------------------------------------------------------------
# catalina.sh - Start/Stop Script for the CATALINA Server
#
# Environment Variable Prequisites
#
#   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
#                 If not present, the current working directory is assumed.
#
#   CATALINA_OPTS (Optional) Java runtime options used when the "start",
#                 "stop", or "run" command is executed.
#
#   JAVA_HOME     Must point at your Java Development Kit installation.
#
# $Id: catalina.sh,v 1.6 2001/06/07 01:41:25 jon Exp $
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ -z "$CATALINA_HOME" ] ; then
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
  
  CATALINA_HOME_1=`dirname "$PRG"`/..
  echo "Guessing CATALINA_HOME from catalina.sh to ${CATALINA_HOME_1}" 
    if [ -d ${CATALINA_HOME_1}/conf ] ; then 
	CATALINA_HOME=${CATALINA_HOME_1}
	echo "Setting CATALINA_HOME to $CATALINA_HOME"
    fi
fi

if [ -z "$CATALINA_OPTS" ] ; then
  CATALINA_OPTS=""
fi

if [ -z "$JAVA_HOME" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

# ----- Set Up The System Classpath -------------------------------------------

CP="$CATALINA_HOME/bin/bootstrap.jar"

if [ -f "$JAVA_HOME/lib/tools.jar" ] ; then
  CP=$CP:"$JAVA_HOME/lib/tools.jar"
fi

# convert the existing path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CP=`cygpath --path --windows "$CP"`
   CATALINA_HOME=`cygpath --path --windows "$CATALINA_HOME"`
fi

echo "Using CLASSPATH: $CP"
echo "Using CATALINA_HOME: $CATALINA_HOME"


# ----- Execute The Requested Command -----------------------------------------

if [ "$1" = "debug" ] ; then

  shift
  pushd $CATALINA_HOME
  if [ "$1" = "-security" ] ; then
    shift
    $JAVA_HOME/bin/jdb \
       $CATALINA_OPTS \
       -sourcepath ../../jakarta-tomcat-4.0/catalina/src/share \
       -classpath $CP -Dcatalina.home=$CATALINA_HOME \
       org.apache.catalina.startup.Bootstrap "$@" start
  else
    $JAVA_HOME/bin/jdb \
       $CATALINA_OPTS \
       -sourcepath ../../jakarta-tomcat-4.0/catalina/src/share \
       -classpath $CP -Dcatalina.home=$CATALINA_HOME \
       org.apache.catalina.startup.Bootstrap "$@" start
  fi
  popd

elif [ "$1" = "embedded" ] ; then

  shift
  for i in ${CATALINA_HOME}/server/lib/*.jar ; do
    CP=$i:${CP}
  done
  for i in ${CATALINA_HOME}/common/lib/*.jar ; do
    CP=$i:${CP}
  done
  echo Embedded Classpath: $CP
  $JAVA_HOME/bin/java $CATALINA_OPTS -classpath $CP \
   -Dcatalina.home=$CATALINA_HOME \
   org.apache.catalina.startup.Embedded "$@"

elif [ "$1" = "env" ] ; then

  export BP CATALINA_HOME CP
  exit 0

elif [ "$1" = "run" ] ; then

  shift
  if [ "$1" = "-security" ] ; then
    echo Using Security Manager
    shift
    $JAVA_HOME/bin/java $CATALINA_OPTS -classpath $CP \
     -Djava.security.manager \
     -Djava.security.policy==$CATALINA_HOME/conf/catalina.policy \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start
  else
    $JAVA_HOME/bin/java $CATALINA_OPTS -classpath $CP \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start
  fi

elif [ "$1" = "start" ] ; then

  shift
  touch $CATALINA_HOME/logs/catalina.out
  if [ "$1" = "-security" ] ; then
    echo Using Security Manager
    shift
    $JAVA_HOME/bin/java $CATALINA_OPTS -classpath $CP \
     -Djava.security.manager \
     -Djava.security.policy==$CATALINA_HOME/conf/catalina.policy \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start \
     >> $CATALINA_HOME/logs/catalina.out 2>&1 &
  else
    $JAVA_HOME/bin/java $CATALINA_OPTS -classpath $CP \
     -Dcatalina.home=$CATALINA_HOME \
     org.apache.catalina.startup.Bootstrap "$@" start \
     >> $CATALINA_HOME/logs/catalina.out 2>&1 &
  fi

elif [ "$1" = "stop" ] ; then

  shift
  $JAVA_HOME/bin/java $CATALINA_OPTS -classpath $CP \
   -Dcatalina.home=$CATALINA_HOME \
   org.apache.catalina.startup.Bootstrap "$@" stop

else

  echo "Usage: catalina.sh ( env | run | start | stop)"
  echo "Commands:"
  echo "  debug             Start Catalina in a debugger"
  echo "  debug -security   Debug Catalina with a security manager"
  echo "  env               Set up environment variables that would be used"
  echo "  run               Start Catalina in the current window"
  echo "  run -security     Start in the current window with security manager"
  echo "  start             Start Catalina in a separate window"
  echo "  start -security   Start in a separate window with security manager"
  echo "  stop -            Stop Catalina"
  exit 1

fi
