#!/bin/sh
##
##  Invokes a script of the same name in the 'tools' module.
##  
##  The 'tools' module is expected to be a peer directory of the directory
##  in which this script lives.
##
##  @author Jason Dillon <jason@planet57.com>
##

# $Id: build.sh,v 1.16 2002/10/05 12:29:11 user57 Exp $

PROGNAME=`basename $0`
DIRNAME=`dirname $0`

# Buss it yo
main() {
    if [ "x$TOOLS_ROOT" = "x" ]; then
	TOOLS_ROOT=`cd $DIRNAME/../tools; pwd`
    fi

    MODULE_ROOT=`cd $DIRNAME; pwd`
    export TOOLS_ROOT MODULE_ROOT DEBUG TRACE

    # Where is the target file?
    target="$TOOLS_ROOT/bin/$PROGNAME"
    if [ ! -f "$target" ]; then
	echo "${PROGNAME}: <ERROR> The target executable does not exist: $target"
	echo
	echo "${PROGNAME}: Please make sure you have checked out the 'tools' module"
	echo "${PROGNAME}: and make sure it is up to date."
        exit 1
    fi

    # Get busy yo!
    if [ "x$DEBUG" != "x" ]; then
	echo "${PROGNAME}: Executing: /bin/sh $target $@"
    fi
    if [ "x$TRACE" = "x" ]; then
	exec /bin/sh $target $@
    else
	exec /bin/sh -x $target $@
    fi
}

# Lets get ready to rumble!
main "$@"
