#! /bin/sh
#
# $Id: startup.sh,v 1.4 2001/09/17 04:59:32 costin Exp $

# Shell script to startup the server

BASEDIR=`dirname $0`

$BASEDIR/tomcat.sh start $@
