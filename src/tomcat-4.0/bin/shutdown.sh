#!/bin/sh
# -----------------------------------------------------------------------------
# shutdown.sh - Stop Script for the CATALINA Server
#
# $Id: shutdown.sh,v 1.11 2001/09/21 19:35:59 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh stop "$@"
