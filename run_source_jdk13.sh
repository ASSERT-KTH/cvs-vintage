#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/ext/xalan.jar:"$COLUMBA_HOME"/ext/jaxp.jar:"$COLUMBA_HOME"/ext/parser.jar:"$COLUMBA_HOME"/ext/jython.jar:"$COLUMBA_HOME"/ext/log4j.jar org.columba.main.Main
