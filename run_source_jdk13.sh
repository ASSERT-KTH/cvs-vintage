#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/lib/xalan.jar:"$COLUMBA_HOME"/lib/jaxp.jar:"$COLUMBA_HOME"/lib/jython.jar:"$COLUMBA_HOME"/lib/log4j.jar org.columba.main.Main
