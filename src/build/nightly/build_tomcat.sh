#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

if [ "$1" != "" ] ; then
  SUFIX=$1
fi
######## MAIN 

ant_build jakarta-tomcat tomcat tomcat-3.3-build-$SUFIX.log "main watchdog-web-based dist"

fix_tomcat

zip_dist tomcat tomcat-3.3-$SUFIX 


