#!/bin/sh
# -----------------------------------------------------------------------------
# startup.sh - Start Script for the CATALINA Server
#
# $Id: startup.sh,v 1.11 2001/09/21 19:35:59 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh start "$@"
