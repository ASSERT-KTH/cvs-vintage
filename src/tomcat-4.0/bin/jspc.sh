#!/bin/sh
# -----------------------------------------------------------------------------
# jspc.sh - Script ro run the Jasper "offline JSP compiler"
#
# $Id: jspc.sh,v 1.9 2001/08/21 18:42:34 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/jasper.sh jspc "$@"
