#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

PATH=$HOME/bin/nightly:$PATH


# Special builds - no ext 
CLASSPATH=.
JAVA_HOME=$HOME/java/java1.2 
TOMCAT_HOME=/home/costin/ws/dist/tomcat
export TOMCAT_HOME
export CLASSPATH
export JAVA_HOME

cvs_update jakarta-tomcat
zip_src jakarta-tomcat tomcat-3.3-src.zip

echo rm -f $LOGDIR/*
rm -f  $LOGDIR/*
echo BUILD `date` >$LOGDIR/nightly.log
build_tomcat  3.3-noext "main watchdog-web-based dist" >>$LOGDIR/nightly.log 2>&1
run_watchdog.sh noext >>$LOGDIR/nightly.log 2>&1
echo BUILD `date` >>$LOGDIR/nightly.log

# Special tomcat build - 1.1
JAVA_HOME=$HOME/java/java1.1 
LD_LIBRARY_PATH=$JAVA_HOME/lib:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH
CLASSPATH=.
echo BUILD `date` >>$LOGDIR/nightly.log
build_tomcat.sh 3.3-jdk11 "main watchdog-web-based dist" >>$LOGDIR/nightly.log 2>&1
# run_watchdog.sh jdk11 >>$LOGDIR/nightly.log 2>&1
rm -rf $WS/dist/tomcat-1.1
echo mv $WS/dist/tomcat $WS/dist/tomcat-1.1
mv $WS/dist/tomcat $WS/dist/tomcat-1.1
echo done
#echo BUILD `date` >>$LOGDIR/nightly.log
#unset LD_LIBARY_PATH

# Full build 
CLASSPATH=$EXTENSIONS
JAVA_HOME=$HOME/java/java1.2 

echo $EXT
echo BUILD `date` >>$LOGDIR/nightly.log
build_tomcat.sh 3.3-full "main watchdog-web-based dist" >>$LOGDIR/nightly.log 2>&1
run_watchdog.sh full >>$LOGDIR/nightly.log 2>&1
echo security test
run_watchdog.sh security >>$LOGDIR/nightly.log 2>&1
echo security done
echo BUILD `date` >>$LOGDIR/nightly.log

# mail -s "Tomcat3.3 nightly build " \
# 	cmanolache@yahoo.com \
#	< $LOGDIR/nightly.log


