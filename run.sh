#!/bin/sh
PATH=/usr/bin

/usr/java/jdk1.3.1/bin/java  -cp .:./ext/jaxp.jar:./ext/parser.jar org/columba/main/Main --path ~/.stable
