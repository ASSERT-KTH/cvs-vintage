#!/bin/sh
# -----------------------------------------------------------------------------
# jspc.sh - Script ro run the Jasper "offline JSP compiler"
#
# $Id: jspc.sh,v 1.7 2001/06/28 01:49:51 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/jasper.sh jspc "$@"
