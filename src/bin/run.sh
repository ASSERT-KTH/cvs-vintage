#!/bin/sh

# Minimal jar file to get JBoss started.

JBOSS_CLASSPATH=$JBOSS_CLASSPATH:run.jar

# Add all login modules for JAAS-based security
# and all libraries that are used by them here
JBOSS_CLASSPATH=$JBOSS_CLASSPATH

# Add the XML parser jars and set the JAXP factory names
# Crimson parser JAXP setup(default)
JBOSS_CLASSPATH=$JBOSS_CLASSPATH:../lib/crimson.jar
JAXP=-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
JAXP="$JAXP -Djavax.xml.parsers.SAXParserFactory=org.apache.crimson.jaxp.SAXParserFactoryImpl"

echo JBOSS_CLASSPATH=$JBOSS_CLASSPATH
java -server $JAXP -classpath $JBOSS_CLASSPATH org.jboss.Main $@

