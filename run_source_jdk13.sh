#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/lib/xalan.jar:"$COLUMBA_HOME"/lib/jaxp.jar:"$COLUMBA_HOME"/lib/jython.jar:"$COLUMBA_HOME"/lib/log4j.jar:"$COLUMBA_HOME"/lib/jargs.jar:"$COLUMBA_HOME"/lib/jakarta-oro-2.0.6.jar:"$COLUMBA_HOME"/lib/lucene-1.2.jar:"$COLUMBA_HOME"/langpack.jar org.columba.core.main.Main
