#!/bin/sh

# Minimal jar file to get JBoss started.

CLASSPATH=$CLASSPATH:run.jar

# Add all login modules for JAAS-based security
# and all libraries that are used by them here
CLASSPATH=$CLASSPATH

# Add the XML parser jars and set the JAXP factory names
# Crimson parser JAXP setup(default)
CLASSPATH=$CLASSPATH:../lib/crimson.jar
JAXP=-Djavax.xml.parsers.DocumentBuilderFactory=org.apache.crimson.jaxp.DocumentBuilderFactoryImpl
JAXP="$JAXP -Djavax.xml.parsers.SAXParserFactory=org.apache.crimson.jaxp.SAXParserFactoryImpl"

echo CLASSPATH=$CLASSPATH
java -server $JAXP -classpath $CLASSPATH org.jboss.Main $@

