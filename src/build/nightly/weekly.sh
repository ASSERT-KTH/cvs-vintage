#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

PATH=$HOME/bin/nightly:$PATH

echo "BUILD `date`" >$LOGDIR/weekly.log

CLASSPATH=$EXTENSIONS 
JAVA_HOME=$HOME/java/java1.2 
export CLASSPATH
export JAVA_HOME

cvs_get jakarta-tomcat >>$LOGDIR/weekly.log 2>&1 
cvs_get jakarta-watchdog>>$LOGDIR/weekly.log 2>&1
cvs_get jakarta-tools>>$LOGDIR/weekly.log 2>&1
cvs_get jakarta-servletapi>>$LOGDIR/weekly.log 2>&1

zip_src jakarta-tomcat tomcat-3.3-src.zip>>$LOGDIR/weekly.log 2>&1
zip_src jakarta-watchdog watchdog-src.zip>>$LOGDIR/weekly.log 2>&1
zip_src jakarta-servletapi servletapi-src.zip>>$LOGDIR/weekly.log 2>&1

ANT_HOME=$HOME/opt/ant-1.1
export ANT_HOME
# Both servlet api and watchdog need ant1.1

echo Building watchdog >>$LOGDIR/weekly.log 2>&1
echo --------------------------------------------------- >>$LOGDIR/weekly.log 2>&1

ant_build jakarta-servletapi servletapi servletapi-build.log >>$LOGDIR/weekly.log 2>&1
ant_build jakarta-watchdog watchdog watchdog-build.log >>$LOGDIR/weekly.log 2>&1
tail -6 $LOGDIR/watchdog-build.log >>$LOGDIR/weekly.log 2>&1

