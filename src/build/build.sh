#! /bin/sh

# $Id: build.sh,v 1.3 2001/03/14 21:58:39 kimptonc Exp $


TARGET_CLASSPATH=`echo ../../lib/*.jar | tr ' ' ':'`

TARGET_CLASSPATH=${TARGET_CLASSPATH}:../../build/classes

java -classpath $TARGET_CLASSPATH org.apache.tools.ant.Main $*
