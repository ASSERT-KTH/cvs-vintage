#! /bin/sh

# $Id: build.sh,v 1.2 2000/12/07 20:05:33 tobias Exp $

TARGET_CLASSPATH=../../lib/ant.jar:\
../../lib/jaxp.jar:\
../../lib/parser.jar:\
../../lib/xmlbeans.jar:\
../../build/classes:\
../../lib/javac.jar

java -classpath $TARGET_CLASSPATH org.apache.tools.ant.Main $*
