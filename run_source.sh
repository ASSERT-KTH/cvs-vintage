#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/classes:"$COLUMBA_HOME"/lib/log4j.jar:"$COLUMBA_HOME"/lib/jargs.jar:"$COLUMBA_HOME"/lib/jakarta-oro-2.0.6.jar:"$COLUMBA_HOME"/lib/lucene-1.2.jar org.columba.core.main.Main
