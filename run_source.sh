#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME":"$COLUMBA_HOME"/classes:"$COLUMBA_HOME"/lib/log4j.jar:"$COLUMBA_HOME"/lib/jargs.jar:"$COLUMBA_HOME"/lib/jakarta-oro-2.0.6.jar:"$COLUMBA_HOME"/lib/lucene-1.3-rc1.jar:"$COLUMBA_HOME"/lib/jwizz-0.1.1.jar:"$COLUMBA_HOME"/lib/plastic.jar:"$COLUMBA_HOME"/lib/jhall.jar:"$COLUMBA_HOME"/lib/usermanual.jar:"$COLUMBA_HOME"/lib/forms.jar:"$COLUMBA_HOME"/lib/ristretto-1.0pre1.jar org.columba.core.main.Main $@
