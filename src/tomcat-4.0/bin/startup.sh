#!/bin/sh
# -----------------------------------------------------------------------------
# startup.sh - Start Script for the CATALINA Server
#
# $Id: startup.sh,v 1.10 2001/09/19 20:19:56 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh start "$@"
