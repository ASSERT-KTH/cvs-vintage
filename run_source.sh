#!/bin/sh

COLUMBA_HOME=`dirname $0`
java -cp "$COLUMBA_HOME" $@ org.columba.main.Main