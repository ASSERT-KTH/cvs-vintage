#!/bin/sh
# -----------------------------------------------------------------------------
# shutdown.sh - Stop Script for the CATALINA Server
#
# $Id: shutdown.sh,v 1.2 2001/02/23 21:51:27 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh stop "$@"
