#!/bin/sh
cd /home/frd/src/columba
java  -cp .:./ext/jaxp.jar:./ext/parser.jar org/columba/main/Main --path /home/frd/.stable/
