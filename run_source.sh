#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/classes:"$COLUMBA_HOME"/lib/jargs.jar:"$COLUMBA_HOME"/lib/lucene-1.3-final.jar:"$COLUMBA_HOME"/lib/jwizz-0.1.1.jar:"$COLUMBA_HOME"/lib/plastic-1.2.0.jar:"$COLUMBA_HOME"/lib/jhall.jar:"$COLUMBA_HOME"/lib/usermanual.jar:"$COLUMBA_HOME"/lib/forms-1.0.3.jar:"$COLUMBA_HOME"/lib/ristretto-1.0pre1.jar org.columba.core.main.Main $@
