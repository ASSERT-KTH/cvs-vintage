#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

# override ANT_HOME
ANT_HOME=$HOME/opt/ant-1.1
export ANT_HOME

######## MAIN 

echo Building watchdog
echo ---------------------------------------------------

ant_build jakarta-servletapi servletapi servletapi-build.log
ant_build jakarta-watchdog watchdog watchdog-build.log
tail -6 $LOGDIR/watchdog-build.log

echo

