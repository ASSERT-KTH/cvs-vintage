#!/bin/sh
# -----------------------------------------------------------------------------
# shutdown.sh - Stop Script for the CATALINA Server
#
# $Id: shutdown.sh,v 1.7 2001/06/28 01:49:51 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh stop "$@"
