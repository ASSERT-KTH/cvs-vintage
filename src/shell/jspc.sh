#!/bin/sh
#
# $Id: jspc.sh,v 1.6 2002/04/18 13:58:01 keith Exp $

# Shell script to runt JspC


BASEDIR=`dirname $0`

$BASEDIR/tomcat jspc "$@"
