#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/ext/xalan.jar:"$COLUMBA_HOME"/ext/jaxp.jar:"$COLUMBA_HOME"/ext/parser.jar org.columba.main.Main
