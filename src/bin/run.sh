#!/bin/sh

# Minimal jar file to get JBoss started.

CLASSPATH=$CLASSPATH:run.jar

# Add all login modules for JAAS-based security
# and all libraries that are used by them here
CLASSPATH="$CLASSPATH:../lib/jdbc2_0-stdext.jar:../lib/jboss-jaas.jar"

echo $CLASSPATH
java -server -classpath $CLASSPATH org.jboss.Main $@
