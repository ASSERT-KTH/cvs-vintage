#!/bin/sh
# -----------------------------------------------------------------------------
# shutdown.sh - Stop Script for the CATALINA Server
#
# $Id: shutdown.sh,v 1.9 2001/08/21 18:42:34 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh stop "$@"
