#!/bin/sh
# -----------------------------------------------------------------------------
# jspc.sh - Script ro run the Jasper "offline JSP compiler"
#
# $Id: jspc.sh,v 1.4 2001/04/08 21:53:58 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/jasper.sh jspc "$@"
