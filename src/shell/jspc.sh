#!/bin/sh
#
# $Id: jspc.sh,v 1.4 2001/08/21 05:43:23 costin Exp $

# Shell script to runt JspC


BASEDIR=`dirname $0`

$BASEDIR/tomcat.sh start "$@"
