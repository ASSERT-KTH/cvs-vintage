#!/bin/sh

# Minimal jar file to get JBoss started.

#assumed that this script will start in the bin directory

#but just in case use JBOSS_HOME if found
if [ "$JBOSS_HOME" ]; then
   cd $JBOSS_HOME/bin
   echo Using JBOSS_HOME to set current directory to $JBOSS_HOME/bin
fi 

JBOSS_CLASSPATH=$JBOSS_CLASSPATH:run.jar

# Add all login modules for JAAS-based security
# and all libraries that are used by them here
JBOSS_CLASSPATH=$JBOSS_CLASSPATH

# Check for SUN(tm) JVM w/ HotSpot support
#
HOTSPOT=`java -version 2>&1 | grep HotSpot`"x"
if [ "$HOTSPOT" != "x" ]; then
       HOTSPOT="-server"
else
       HOTSPOT=""
fi

# Add the XML parser jars and set the JAXP factory names
# Crimson parser JAXP setup(default)
JBOSS_CLASSPATH=$JBOSS_CLASSPATH:../lib/crimson.jar
JAXP=-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
JAXP="$JAXP -Djavax.xml.parsers.SAXParserFactory=org.apache.crimson.jaxp.SAXParserFactoryImpl"

echo JBOSS_CLASSPATH=$JBOSS_CLASSPATH
java $HOTSPOT $JAXP -classpath $JBOSS_CLASSPATH org.jboss.Main $@

