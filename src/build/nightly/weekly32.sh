#!/bin/sh

WS=/home/costin/ws32
export WS

. $HOME/bin/nightly/functions_build.sh

PATH=$HOME/bin/nightly:$PATH

echo "BUILD `date`" >$LOGDIR/weekly.log

CLASSPATH=$EXTENSIONS 
JAVA_HOME=$HOME/java/java1.2 
export CLASSPATH
export JAVA_HOME

cvs_get jakarta-tomcat "-r tomcat_32" >>$LOGDIR/weekly.log 2>&1 
cvs_get jakarta-watchdog "-r tomcat_32"  >>$LOGDIR/weekly.log 2>&1
cvs_get jakarta-tools>>$LOGDIR/weekly.log 2>&1
cvs_get jakarta-servletapi>>$LOGDIR/weekly.log 2>&1

zip_src jakarta-tomcat tomcat-3.2-src.zip>>$LOGDIR/weekly.log 2>&1
zip_src jakarta-watchdog watchdog-src-32.zip>>$LOGDIR/weekly.log 2>&1
zip_src jakarta-servletapi servletapi-src.zip>>$LOGDIR/weekly.log 2>&1

ant_build jakarta-servletapi servletapi servletapi-build.log
tail -6 $LOGDIR/servletapi-build.log

ANT_HOME=$HOME/opt/ant-1.1
export ANT_HOME
ant_build jakarta-watchdog watchdog watchdog-build.log
ANT_HOME=$HOME/opt/ant-1.2
export ANT_HOME

tail -6 $LOGDIR/watchdog-build.log

