#!/bin/sh
#
# $Id: jspc.sh,v 1.5 2001/09/27 12:40:35 hgomez Exp $

# Shell script to runt JspC


BASEDIR=`dirname $0`

$BASEDIR/tomcat.sh jspc "$@"
