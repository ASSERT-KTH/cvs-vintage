#! /bin/sh

# $Id: build.sh,v 1.3 1999/11/28 23:52:29 harishp Exp $

cp=../jakarta-tools/ant.jar:../jakarta-tools/projectx-tr2.jar:../build/tomcat/classes

java -classpath $cp:$CLASSPATH org.apache.tools.ant.Main "$@"

chmod +x `find ../build -name "*.sh"`
