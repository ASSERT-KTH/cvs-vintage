#! /bin/sh
#
# $Id: startup.sh,v 1.5 2002/04/18 13:58:01 keith Exp $

# Shell script to startup the server

BASEDIR=`dirname $0`

$BASEDIR/tomcat start $@
