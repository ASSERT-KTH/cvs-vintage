#!/bin/sh
#
# Copyright 2001-2004 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Shell script to run the tomcat sanity test suite 
 
if [ "$TOMCAT_HOME" = "" ] ; then
    echo You need to set TOMCAT_HOME
    exit
fi

cp=$CLASSPATH

CLASSPATH=${TOMCAT_HOME}/lib/container/parser.jar
CLASSPATH=${TOMCAT_HOME}/lib/container/crimson.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/lib/container/xerces.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/lib/container/jaxp.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/webapps/admin/WEB-INF/lib/ant.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/webapps/admin/WEB-INF/lib/tomcat_util_test.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/webapps/admin/WEB-INF/classes:$CLASSPATH

if [ "$cp" != "" ] ; then
    CLASSPATH=${CLASSPATH}:${cp}
fi

export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

ant -Dgdir=${TOMCAT_HOME}/webapps/test/Golden -f ${TOMCAT_HOME}/webapps/test/WEB-INF/test-tomcat.xml client $*

exit 0
