#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

######## MAIN 

ROOT=$WS/dist
if [ "$1" = "-b" ] ; then 
    ROOT=$WS/build
fi

CLASSPATH=$ANT_HOME/lib/parser.jar:$CLASSPATH
CLASSPATH=$ANT_HOME/lib/jaxp.jar:$CLASSPATH
CLASSPATH=$ANT_HOME/lib/ant.jar:$CLASSPATH
export CLASSPATH
cd $ROOT

# Make sure no tomcat is running
cd $ROOT/tomcat
bin/tomcat.sh stop 

if [ -f /usr/bin/pkill ] ; then 
   pkill java
fi

if [ -f /usr/bin/killall ] ; then 
   killall -9 java
fi


