#! /bin/sh
#
# $Id: shutdown.sh,v 1.3 2001/08/22 04:55:33 costin Exp $

# Shell script to shutdown the server

BASEDIR=`dirname $0`

$BASEDIR/tomcat.sh stop "$@"
