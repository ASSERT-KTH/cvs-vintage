#!/bin/sh

# set tomcat env 
. bin/tomcat.sh env

ant -Dtomcat.home $TOMCAT_HOME -f conf/test-tomcat.xml $@ 
