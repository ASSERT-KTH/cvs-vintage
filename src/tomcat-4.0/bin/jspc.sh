#!/bin/sh
# -----------------------------------------------------------------------------
# jspc.sh - Script ro run the Jasper "offline JSP compiler"
#
# $Id: jspc.sh,v 1.2 2001/02/23 21:51:27 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/jasper.sh jspc "$@"
