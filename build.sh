#! /bin/sh

# $Id: build.sh,v 1.2 1999/11/25 20:42:10 harishp Exp $

cp=../jakarta-tools/ant.jar:../jakarta-tools/projectx-tr2.jar:../build/tomcat/classes

java -classpath $cp:$CLASSPATH org.apache.tools.ant.Main $*

chmod +x `find ../build -name "*.sh"`
