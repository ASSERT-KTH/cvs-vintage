#! /bin/sh

# $Id: build.sh,v 1.1 1999/10/09 00:19:57 duncan Exp $

cp=../jakarta-tools/ant.jar:../jakarta-tools/projectx-tr2.jar

java -classpath $cp:$CLASSPATH org.apache.tools.ant.Main $*
