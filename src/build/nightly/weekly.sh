#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

PATH=$HOME/bin/nightly:$PATH

echo "BUILD `date`" >$LOGDIR/weekly.log

CLASSPATH=$EXTENSIONS 
JAVA_HOME=$HOME/java/java1.2 
export CLASSPATH
export JAVA_HOME

cvs_get.sh -f all >>$LOGDIR/weekly.log 2>&1
build_once.sh >>$LOGDIR/weekly.log 2>&1
build_tomcat.sh full >>$LOGDIR/weekly.log 2>&1





