#!/bin/sh

# Minimal jar file to get JBoss started.

CLASSPATH=$CLASSPATH:run.jar

if [ "${TOMCAT_HOME}X" != "X" ] ; then
    if [ -x $TOMCAT_HOME ] ; then
        echo "Adding jar files in ${TOMCAT_HOME}/lib to CLASSPATH"
        CLASSPATH="$CLASSPATH:${TOMCAT_HOME}/lib"
        CLASSPATH="$CLASSPATH:${TOMCAT_HOME}/lib/servlet.jar"
        CLASSPATH="$CLASSPATH:${TOMCAT_HOME}/lib/webserver.jar"
        CLASSPATH="$CLASSPATH:${TOMCAT_HOME}/lib/xml.jar"
        CLASSPATH="$CLASSPATH:${TOMCAT_HOME}/lib/jaxp.jar"
        CLASSPATH="$CLASSPATH:${TOMCAT_HOME}/lib/parser.jar"
        CLASSPATH="$CLASSPATH:${TOMCAT_HOME}/lib/jasper.jar"

        # Add the tools.jar file so that Tomcat can find the Java compiler.
        CLASSPATH="$CLASSPATH:$JAVA_HOME/lib/tools.jar"
    else 
        echo "TOMCAT_HOME is set, but is an invalid directory"
    fi
fi

if [ "${SPYDERMQ_HOME}X" != "X" ] ; then
    if [ -x $SPYDERMQ_HOME ] ; then
        echo "Adding jar files in ${SPYDERMQ_HOME}/lib to CLASSPATH"
        CLASSPATH="$CLASSPATH:${SPYDERMQ_HOME}/lib/spydermq.jar"
        CLASSPATH="$CLASSPATH:${SPYDERMQ_HOME}/lib/jms.jar"
        CLASSPATH="$CLASSPATH:${SPYDERMQ_HOME}/lib/jnpserver.jar"
    else 
        echo "SPYDERMQ_HOME is set, but is an invalid directory"
    fi
fi

# Add the $JBOSS_HOME/conf to the classpath so that 
# the ClassLoader.getResource() will find the relevant
# configuration and resource files.

if [ "${JBOSS_HOME}X" != "X" ] ; then
    CLASSPATH="$CLASSPATH:$JBOSS_HOME/conf"
fi

echo $CLASSPATH
java -server -classpath $CLASSPATH -Dtomcat.home=$TOMCAT_HOME org.jboss.Main
