#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/classes:"$COLUMBA_HOME"/lib/log4j.jar:"$COLUMBA_HOME"/lib/jargs.jar:"$COLUMBA_HOME"/lib/jakarta-oro-2.0.6.jar:"$COLUMBA_HOME"/lib/lucene-1.3-rc1.jar:"$COLUMBA_HOME"/lib/JWizard-0.1.0-alpha.jar org.columba.core.main.Main $@
