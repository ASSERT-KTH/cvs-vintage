#!/bin/sh
#
# JBoss Control Script
#
# chkconfig: 3 80 20
# description: JBoss EJB Container
# 
# To use this script
# run it as root - it will switch to the specified user
# It loses all console output - use the log.
#
# Here is a little (and extremely primitive) 
# startup/shutdown script for RedHat systems. It assumes 
# that JBoss lives in /usr/local/jboss, it's run by user 
# 'jboss' and JDK binaries are in /usr/local/jdk/bin. All 
# this can be changed in the script itself. 
# Bojan 
#
# Either amend this script for your requirements
# or just ensure that the following variables are set correctly 
# before calling the script

# [ #420297 ] JBoss startup/shutdown for RedHat


JBOSS_HOME=${JBOSS_HOME:-"/usr/local/jboss"}
JAVAPTH=${JAVAPTH:-"/usr/local/jdk/bin"}

JBOSSCP=${JBOSSCP:-"$JBOSS_HOME/lib/ext/jboss.jar"}
JBOSSSH=${JBOSSSH:-"$JBOSS_HOME/bin/run.sh"}
JBOSSUS=${JBOSSUS:-"jboss"}

if [ -z "`echo $PATH | grep $JAVAPTH`" ]; then
  export PATH=$PATH:$JAVAPTH
fi

if [ ! -d "$JBOSS_HOME" ]; then
  echo JBOSS_HOME does not exist as a valid directory : $JBOSS_HOME
  exit 1
fi

case "$1" in
start)
    su - $JBOSSUS -c "cd $JBOSS_HOME/bin; $JBOSSSH >/dev/null 2>&1 &"
    ;;
stop)
    su - $JBOSSUS -c "java -classpath $JBOSSCP org.jboss.Shutdown"
    ;;
restart)
    $0 stop
    $0 start
    ;;
*)
    echo "usage: $0 (start|stop|restart|help)"
esac


