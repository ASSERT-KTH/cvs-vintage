#!/bin/sh
#

# Shell script to run the tomcat sanity test suite 
 
if [ "$TOMCAT_HOME" = "" ] ; then
    echo You need to set TOMCAT_HOME
    exit
fi

cp=$CLASSPATH

CLASSPATH=${TOMCAT_HOME}/lib/container/tomcat_util.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/lib/common/core_util.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/lib/container/parser.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/lib/container/jaxp.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/webapps/admin/WEB-INF/lib/ant.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/webapps/admin/WEB-INF/classes:$CLASSPATH

if [ "$cp" != "" ] ; then
    CLASSPATH=${CLASSPATH}:${cp}
fi

export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

ant -Dgdir=${TOMCAT_HOME}/webapps/test/Golden -f ${TOMCAT_HOME}/webapps/test/WEB-INF/test-tomcat.xml client $*

exit 0
