#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

PATH=$HOME/bin/nightly:$PATH


# Special builds - no ext 
CLASSPATH=.
JAVA_HOME=$HOME/java/java1.2 
TOMCAT_HOME=/home/costin/ws/jakarta-tomcat/dist/tomcat
export TOMCAT_HOME
export CLASSPATH
export JAVA_HOME

cvs_get jakarta-tomcat
zip_src jakarta-tomcat tomcat-3.3-src.zip

rm -f  $LOGDIR/*

echo Build 3.3 noext + test web apps `date` >$LOGDIR/nightly.log
ant_build jakarta-tomcat tomcat tomcat-build-noext.log "dist tests.dist" >>$LOGDIR/nightly.log 2>&1

cp $TOMCAT_HOME/webapps/test.war $WS/zip
cp $TOMCAT_HOME/webapps/jsp-tests.war $WS/zip
cp $TOMCAT_HOME/webapps/servlet-tests.war $WS/zip

echo Watchdog/3.3 noext start `date` >>$LOGDIR/nightly.log
run_watchdog.sh noext >>$LOGDIR/nightly.log 2>&1
echo Watchdog end `date` >>$LOGDIR/nightly.log

# Special tomcat build - 1.1
JAVA_HOME=$HOME/java/java1.1 
LD_LIBRARY_PATH=$JAVA_HOME/lib:$LD_LIBRARY_PATH
export LD_LIBRARY_PATH
CLASSPATH=.
echo BUILD `date` >>$LOGDIR/nightly.log

ant_build jakarta-tomcat tomcat tomcat-build-3.3-jdk11.log dist >>$LOGDIR/nightly.log 2>&1

# run_watchdog.sh jdk11 >>$LOGDIR/nightly.log 2>&1
#echo BUILD `date` >>$LOGDIR/nightly.log
#unset LD_LIBARY_PATH

# Full build 
CLASSPATH=$EXTENSIONS
JAVA_HOME=$HOME/java/java1.2 

echo $EXT
echo BUILD `date` >>$LOGDIR/nightly.log
build_tomcat 3.3-full dist >>$LOGDIR/nightly.log 2>&1

cp $WS/zip/test.war  $TOMCAT_HOME/webapps/test.war
cp $WS/zip/jsp-tests.war $TOMCAT_HOME/webapps/jsp-tests.war
cp $WS/zip/servlet-tests.war $TOMCAT_HOME/webapps/servlet-tests.war

run_watchdog.sh full >>$LOGDIR/nightly.log 2>&1
run_watchdog.sh security >>$LOGDIR/nightly.log 2>&1
echo BUILD `date` >>$LOGDIR/nightly.log

