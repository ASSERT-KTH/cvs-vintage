#! /bin/sh
#
# $Id: shutdown.sh,v 1.2 1999/12/05 17:02:42 harishp Exp $

# Shell script to shutdown the server

# There are other, simpler commands to shutdown the runner. The two
# commented commands good replacements. The first works well with
# Java Platform 1.1 based runtimes. The second works well with
# Java2 Platform based runtimes.

#jre -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Shutdown $*
#java -cp runner.jar:servlet.jar:classes org.apache.tomcat.shell.Shutdown $*

BASEDIR=`dirname $0`

$BASEDIR/tomcat.sh stop "$@"
