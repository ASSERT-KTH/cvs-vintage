#!/bin/sh

cd ~/columba
java  -cp .:./ext/jaxp.jar:./ext/parser.jar org/columba/main/Main
