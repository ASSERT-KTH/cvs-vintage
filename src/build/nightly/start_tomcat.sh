#!/bin/sh


. $HOME/bin/nightly/functions_build.sh

######## MAIN 


ROOT=$WS/dist
if [ "$1" = "-b" ] ; then 
    ROOT=$WS/build
    shift
fi

EXT=$1

CLASSPATH=$ANT_HOME/lib/parser.jar:$CLASSPATH
CLASSPATH=$ANT_HOME/lib/jaxp.jar:$CLASSPATH
CLASSPATH=$ANT_HOME/lib/ant.jar:$CLASSPATH
export CLASSPATH
cd $ROOT

echo Start tomcat
echo TOMCAT_HOME=`pwd`
echo LOG=$LOGDIR/tomcat-run$EXT.log
$JAVA_HOME/bin/java -version

# Make sure no tomcat is running
cd $ROOT/tomcat
echo Make sure tomcat is stopped
bin/tomcat.sh stop >/dev/null 2>&1

cd $ROOT/tomcat

rm -f $LOGDIR/tomcat-run$EXT.log >/dev/null 2>&1
$JAVA_HOME/bin/java -version > $LOGDIR/tomcat-run$EXT.log 2>&1 &
if [ "$EXT" = "security" ] ; then
  bin/tomcat.sh run -security >> $LOGDIR/tomcat-run$EXT.log 2>&1 &
else
  bin/tomcat.sh run >> $LOGDIR/tomcat-run$EXT.log 2>&1 &
fi

i=0
while [ "$STARTED" != "0" ] ; do
	sleep 1
	# read
	# echo $REPLY | 
        grep "Http10Interceptor"  $LOGDIR/tomcat-run$EXT.log \
		>/dev/null 2>&1
	STARTED=$?
	i=`expr $i + 1`
        if [ "$i" = "120" ] ; then
	    echo "Can't start after 120 seconds "
            tail -20  $LOGDIR/tomcat-run$EXT.log 
	    exit 1
	fi
	# echo "Wait - not ready $i $STARTED"
	# ls -l $LOGDIR/tomcat-run$EXT.log
done
sleep 2
echo Started ok 


