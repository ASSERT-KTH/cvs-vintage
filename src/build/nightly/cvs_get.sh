#!/bin/sh

. $HOME/bin/nightly/functions_build.sh

GET=""

if [ "$1" = "-f" ]; then
    GET="-f"
    shift
    cvs_get jakarta-tomcat
else
    cvs_update jakarta-tomcat
fi

zip_src jakarta-tomcat tomcat-3.3-src.zip

if [ "$1" = "all" ]; then
  if [ "$GET" = "-f" ]; then
    cvs_get jakarta-watchdog
    cvs_get jakarta-tools
    cvs_get jakarta-servletapi
  else
    cvs_update jakarta-watchdog
    cvs_update jakarta-tools
    cvs_update jakarta-servletapi
  fi
  zip_src jakarta-watchdog watchdog-src.zip
  zip_src jakarta-servletapi servletapi-src.zip
fi

zip_src jakarta-tomcat tomcat-3.3-src.zip
