#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

######## MAIN 

ROOT=$WS/dist
if [ "$1" = "-b" ] ; then 
    ROOT=$WS/build
    BUILD=true
    echo Using build $1
    shift
fi
if [ "$1" = "-s" ] ; then
    NOSTART=true;
    echo No start
    shift
fi

EXT=$1
CLASSPATH=$ANT_HOME/lib/parser.jar:$CLASSPATH
CLASSPATH=$ANT_HOME/lib/jaxp.jar:$CLASSPATH
CLASSPATH=$ANT_HOME/lib/ant.jar:$CLASSPATH
export CLASSPATH

cd $ROOT

cp $ANT_HOME/lib/ant.jar $ROOT/watchdog/lib

#rm -rf tomcat/webapps/jsp-tests >/dev/null 2>&1 
#rm -rf tomcat/webapps/servlet-tests >/dev/null 2>&1 
if [ "$BUILD" != "" ]; then
    ln -s $ROOT/watchdog/webapps/jsp-tests tomcat/webapps >/dev/null 2>&1 
    ln -s $ROOT/watchdog/webapps/servlet-tests tomcat/webapps >/dev/null 2>&1 
else
    ln -s $ROOT/watchdog/webapps/jsp-tests.war tomcat/webapps >/dev/null 2>&1 
    ln -s $ROOT/watchdog/webapps/servlet-tests.war tomcat/webapps >/dev/null 2>&1 
fi

echo ---------- Start  tomcat `date` ---------- 
echo WS=$ROOT EXT=$EXT 

echo Start `date` >$LOGDIR/watchdog-$EXT-1.log 2>&1 

# Port 9080
mv $ROOT/tomcat/conf/server.xml $ROOT/tomcat/conf/server.xml.orig
sed s/8080/9080/ <$ROOT/tomcat/conf/server.xml.orig >$ROOT/tomcat/conf/server.xml

if [ "$NOSTART" = "" ]; then
    if [ "$BUILD" = "" ]; then
	start_tomcat.sh $EXT
	if [ "$?" = "1" ]; then
	    exit 1
	fi
    else
	start_tomcat.sh -b $EXT
	if [ "$?" = "1" ]; then
	    exit 1
	fi
    fi
fi

cd $ROOT/watchdog
WATCHDOG_HOME=`pwd`
export WATCHDOG_HOME

# We know watchdog can fail with JDK1.1. 

echo 
echo ---------- Running watchdog `date` ---------- 
echo JAVA_VERSION `$JAVA_HOME/bin/java -version`

rm -rf $TOMCAT_HOME/work/DEFAULT/jsp-tests/jsp/tagext
time bin/watchdog.sh all localhost 9080 >>$LOGDIR/watchdog-$EXT-1.log 2>&1 

echo Watchdog1 done `date`

count_errors $LOGDIR/watchdog-$EXT-1

$JAVA_HOME/bin/java -version 2>&1 | grep 1.1 >/dev/null 2>&1 
if [ "$?" = "0" ] ; then
    echo Detected java1.1 - run again with 1.2 
    JAVA_HOME=$HOME/java/java1.2
    echo 
    echo Running watchdog  1.2 `date`

    time bin/watchdog.sh all localhost 9080 >$LOGDIR/watchdog-$EXT-1.2.log 2>&1 

    echo Watchdog1.2 done `date`
    count_errors $LOGDIR/watchdog-$EXT-1.2
fi

echo 
echo Running watchdog  2 `date`

rm -rf $TOMCAT_HOME/work/DEFAULT/jsp-tests/jsp/tagext
time bin/watchdog.sh all localhost 9080 >$LOGDIR/watchdog-$EXT-2.log 2>&1

echo Watchdog2 done `date`
count_errors $LOGDIR/watchdog-$EXT-2

echo 
echo Running watchdog 3 `date`

rm -rf $TOMCAT_HOME/work/DEFAULT/jsp-tests/jsp/tagext
time bin/watchdog.sh all localhost 9080  >$LOGDIR/watchdog-$EXT-3.log 2>&1 

echo Watchdog3 done `date`
count_errors $LOGDIR/watchdog-$EXT-3

X=$PATH
PATH=$HOME/opt/ant-1.2/bin:$PATH
export PATH
time sh $TOMCAT_HOME/webapps/admin/WEB-INF/scripts/run-test.sh  -Dport=9080 >$LOGDIR/sanity-$EXT.log 2>&1
count_errors $LOGDIR/sanity-$EXT
PATH=$X
export PATH

if [ "$NOSTART" = "" ]; then
   stop_tomcat.sh >>$LOGDIR/watchdog-$EXT-1.log 2>&1 
fi

