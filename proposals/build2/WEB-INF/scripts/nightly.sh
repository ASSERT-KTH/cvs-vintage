#!/bin/sh

TOMCAT=$HOME/tomcat
S=$TOMCAT/webapps/build/WEB-INF/scripts
H=$TOMCAT/webapps/build
L=$TOMCAT/webapps/build/log
ANT_HOME=$HOME/opt/jakarta-ant-1.3
ANT=$ANT_HOME/bin/ant
DEF="-Dtomcat.home=$TOMCAT -Dant.home=$ANT_HOME"
JAVA_HOME=/usr/java1.3.0
PATH=/home/costin/bin:/usr/local/bin:$JAVA_HOME/bin:$PATH
export PATH
unset ZIP
export JAVA_HOME

$ANT -f $S/tomcat.xml tomcat.clean.src >$L/tomcat.clean.src.log 2>&1

$ANT -f $S/tomcat.xml $DEF -Dpackage.name=jakarta-tomcat -Darch.name=tomcat-3.3 cvs.get >$L/cvs.get.tomcat.log 2>&1

$ANT -f $S/tomcat.xml $DEF -Dpackage.name=jakarta-tomcat -Darch.name=tomcat-3.3 src.package >$L/src.package.tomcat.log 2>&1

$ANT -f $S/tomcat.xml $DEF tomcat-clean >$L/jdk11.log 2>&1

$ANT -f $S/tomcat.xml $DEF tomcat-jdk11 >>$L/jdk11.log 2>&1


$ANT -f $S/tomcat.xml $DEF tomcat-clean >$L/nosse.log 2>&1
$ANT -f $S/tomcat.xml $DEF tomcat-noext >>$L/nosse.log 2>&1

$ANT -f $S/tomcat.xml $DEF tomcat-clean >$L/full.log 2>&1
$ANT -f $S/tomcat.xml $DEF tomcat-jsse >>$L/full.log 2>&1
$ANT -f $S/tomcat.xml $DEF -Darch.name=tomcat bin.package >>$L/full.log 2>&1

$ANT -f $S/tomcat.xml $DEF test.build >>$L/full.log 2>&1

$ANT -f $S/tomcat.xml $DEF change.port >$L/test.log 2>&1

$ANT -f $S/tomcat.xml stop-tomcat >>$L/test.log 2>&1

$ANT -f $S/tomcat.xml start-tomcat >>$L/test.log 2>&1

$ANT -f $S/tomcat.xml run-watchdog >>$L/test.log 2>&1

$ANT -f $S/tomcat.xml run-sanity >>$L/test.log 2>&1

$ANT -f $S/tomcat.xml stop-tomcat >>$L/test.log 2>&1

$ANT -f $S/tomcat.xml start-tomcat-security >$L/test-sandbox.log 2>&1

$ANT -f $S/tomcat.xml run-watchdog >>$L/test-sandbox.log 2>&1

$ANT -f $S/tomcat.xml run-sanity >>$L/test-sandbox.log 2>&1

$ANT -f $S/tomcat.xml stop-tomcat >>$L/test-sandbox.log 2>&1

