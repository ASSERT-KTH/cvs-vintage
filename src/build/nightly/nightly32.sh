#!/bin/sh

WS=/home/costin/ws32
export WS

. $HOME/bin/nightly/functions_build.sh

PATH=$HOME/bin/nightly:$PATH

# will use the sticky tag ( that is already there )
cvs_update jakarta-tomcat

# Special builds - no ext 
CLASSPATH=.
JAVA_HOME=$HOME/java/java1.2 
TOMCAT_HOME=$WS/dist/tomcat
export TOMCAT_HOME
export CLASSPATH
export JAVA_HOME


echo rm -f $LOGDIR/*
rm -f  $LOGDIR/*

# Full build 
CLASSPATH=$EXTENSIONS
export CLASSPATH
echo Building with $CLASSPATH
JAVA_HOME=$HOME/java/java1.2 

echo $EXT
echo BUILD `date` >>$LOGDIR/nightly.log
ant_build jakarta-tomcat tomcat tomcat-3.3-build-full.log dist >>$LOGDIR/nightly.log 2>&1

fix_tomcat

zip_dist tomcat tomcat-3.3-full

run_watchdog.sh full >>$LOGDIR/nightly.log 2>&1

echo security test
run_watchdog.sh security >>$LOGDIR/nightly.log 2>&1
echo security done
echo BUILD `date` >>$LOGDIR/nightly.log

# mail -s "Tomcat3.3 nightly build " \
# 	cmanolache@yahoo.com \
#	< $LOGDIR/nightly.log


