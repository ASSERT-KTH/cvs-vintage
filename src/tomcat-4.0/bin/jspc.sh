#!/bin/sh
# -----------------------------------------------------------------------------
# jspc.sh - Script ro run the Jasper "offline JSP compiler"
#
# $Id: jspc.sh,v 1.10 2001/09/19 20:19:56 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/jasper.sh jspc "$@"
