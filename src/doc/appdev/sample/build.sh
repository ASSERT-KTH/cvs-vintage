#!/bin/sh
# build.sh -- Build Script for the "Hello, World" Application
# $Id: build.sh,v 1.3 2004/02/29 22:42:51 billbarker Exp $
#
#   Copyright 1999-2004 The Apache Software Foundation
# 
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
# 
#       http://www.apache.org/licenses/LICENSE-2.0
# 
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

# Identify the custom class path components we need
CP=$TOMCAT_HOME/webapps/admin/WEB-INF/lib/ant.jar:$TOMCAT_HOME/lib/common/servlet.jar
CP=$CP:$TOMCAT_HOME/lib/container/crimson.jar
CP=$CP:$JAVA_HOME/lib/tools.jar

# Execute ANT to perform the requested build target
java -classpath $CP:$CLASSPATH org.apache.tools.ant.Main \
  -Dtomcat.home=$TOMCAT_HOME "$@"
