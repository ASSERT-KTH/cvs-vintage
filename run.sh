#!/bin/sh
PATH=/usr/bin

cd /home/timo/src/columba
/usr/java/jdk1.3.1_01/bin/java  -cp .:./ext/jaxp.jar:./ext/parser.jar org/columba/main/Main
