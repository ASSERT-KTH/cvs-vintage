#!/bin/sh
#

# Shell script to run sanity test suite
 
if [ "$TOMCAT_HOME" = "" ] ; then
    echo You need to set TOMCAT_HOME
fi

cp=$CLASSPATH

CLASSPATH=${TOMCAT_HOME}/lib/ant.jar:$CLASSPATH
CLASSPATH=${TOMCAT_HOME}/webapps/test/WEB-INF/classes:$CLASSPATH

if [ "$cp" != "" ] ; then
    CLASSPATH=${CLASSPATH}:${cp}
fi

export CLASSPATH

echo Using classpath: ${CLASSPATH}
echo

ant -Dgdir=${TOMCAT_HOME}/webapps/test/Golden -f ${TOMCAT_HOME}/webapps/test/WEB-INF/test-tomcat.xml $*

exit 0
