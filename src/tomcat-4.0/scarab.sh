#!/bin/sh

CATALINA_OPTS="-Xms128M -Xmx256M"
export CATALINA_OPTS

./bin/catalina.sh run
