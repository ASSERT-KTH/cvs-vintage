#! /bin/sh
#
# $Id: shutdown.sh,v 1.4 2001/09/17 04:59:32 costin Exp $

# Shell script to shutdown the server

BASEDIR=`dirname $0`

$BASEDIR/tomcat.sh stop $@
