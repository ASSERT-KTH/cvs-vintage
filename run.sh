#!/bin/sh
PATH=/usr/bin

cd /home/freddy/src/columba
/usr/java/jdk1.3.1/bin/java  -cp .:./ext/jaxp.jar:./ext/parser.jar org/columba/main/Main --path ~/.stable
