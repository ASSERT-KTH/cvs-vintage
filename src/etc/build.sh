#! /bin/sh

# $Id: build.sh,v 1.1 2000/05/14 07:26:16 oberg Exp $

TARGET_CLASSPATH=../../lib/ant.jar:\
../../lib/xml.jar:\
../../lib/xmlbeans.jar:\
../../build/classes:\
../../lib/javac.jar

java -classpath $TARGET_CLASSPATH org.apache.tools.ant.Main $*
