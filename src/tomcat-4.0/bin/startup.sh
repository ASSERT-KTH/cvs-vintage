#!/bin/sh
# -----------------------------------------------------------------------------
# startup.sh - Start Script for the CATALINA Server
#
# $Id: startup.sh,v 1.7 2001/06/28 01:49:51 jon Exp $
# -----------------------------------------------------------------------------

BASEDIR=`dirname $0`
$BASEDIR/catalina.sh start "$@"
