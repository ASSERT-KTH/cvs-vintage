#! /bin/sh

# $Id: build.sh,v 1.13 2001/03/15 20:41:14 larryi Exp $

if [ -z "$JAVA_HOME" ]
then
JAVACMD=`which java`
if [ -z "$JAVACMD" ]
then
echo "Cannot find JAVA. Please set your PATH."
exit 1
fi
JAVA_BINDIR=`dirname $JAVACMD`
JAVA_HOME=$JAVA_BINDIR/..
fi

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant-1.3
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

$ANT_HOME/bin/ant "$@" 
