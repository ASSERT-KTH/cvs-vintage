#!/bin/sh
# -----------------------------------------------------------------------------
# shutdown.sh - Stop Script for the CATALINA Server
#
# $Id: shutdown.sh,v 1.10 2001/09/19 20:19:56 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh stop "$@"
