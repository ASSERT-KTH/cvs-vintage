#!/bin/sh

# Minimal jar file to get JBoss started.

CLASSPATH=$CLASSPATH:run.jar

JETTY_FLAGS=
if [ "${JETTY_HOME}X" != "X" ] ; then
    if [ -x $JETTY_HOME ] ; then
        echo "Adding jar files in ${JETTY_HOME}/lib to CLASSPATH"
        CLASSPATH="$CLASSPATH:${JETTY_HOME}/lib"
        CLASSPATH="$CLASSPATH:${JETTY_HOME}/lib/com.sun.net.ssl.jar"
        CLASSPATH="$CLASSPATH:${JETTY_HOME}/lib/javax.servlet.jar"
        CLASSPATH="$CLASSPATH:${JETTY_HOME}/lib/com.mortbay.jetty.jar"
        CLASSPATH="$CLASSPATH:${JETTY_HOME}/lib/org.apache.jasper.jar"

        # Add the tools.jar file so that Jetty can find the Java compiler.
        CLASSPATH="$CLASSPATH:$JAVA_HOME/lib/tools.jar"
    else 
        echo "JETTY_HOME is set, but is an invalid directory"
    fi
    JETTY_FLAGS="$JETTY_FLAGS -Djetty.home=$JETTY_HOME"
    JETTY_FLAGS="$JETTY_FLAGS -Dorg.xml.sax.parser=com.sun.xml.parser.Parser"
fi

# Add all login modules for JAAS-based security
# and all libraries that are used by them here
CLASSPATH="$CLASSPATH:../lib/jdbc2_0-stdext.jar:../lib/jboss-jaas.jar"

echo $CLASSPATH
java -server -classpath $CLASSPATH $JETTY_FLAGS -Dtomcat.home=$TOMCAT_HOME org.jboss.Main $@
