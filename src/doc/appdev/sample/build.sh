#!/bin/sh
# build.sh -- Build Script for the "Hello, World" Application
# $Id: build.sh,v 1.2 2001/10/21 04:21:36 larryi Exp $

# Identify the custom class path components we need
CP=$TOMCAT_HOME/webapps/admin/WEB-INF/lib/ant.jar:$TOMCAT_HOME/lib/common/servlet.jar
CP=$CP:$TOMCAT_HOME/lib/container/crimson.jar
CP=$CP:$JAVA_HOME/lib/tools.jar

# Execute ANT to perform the requested build target
java -classpath $CP:$CLASSPATH org.apache.tools.ant.Main \
  -Dtomcat.home=$TOMCAT_HOME "$@"
