#!/bin/sh

cd ~/src/columba
java  -cp .:./ext/jaxp.jar:./ext/parser.jar org/columba/main/Main --path ~/.stable
