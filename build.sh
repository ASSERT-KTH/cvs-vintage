#!/bin/sh
##
##  Invokes a script of the same name in the 'tools' module.
##  
##  The 'tools' module is expected to be a peer directory of the directory
##  in which this script lives.
##
##  @author Jason Dillon <jason@planet57.com>
##

# $Id: build.sh,v 1.17 2002/10/07 23:04:12 user57 Exp $

PROGNAME=`basename $0`
DIRNAME=`dirname $0`

# Buss it yo
main() {
    if [ "x$TOOLS_ROOT" = "x" ]; then
	TOOLS_ROOT=`cd $DIRNAME/../tools; pwd`
    fi

    MODULE_ROOT=`cd $DIRNAME; pwd`
    export TOOLS_ROOT MODULE_ROOT DEBUG TRACE

    # Where is the target script?
    target="$TOOLS_ROOT/bin/$PROGNAME"
    if [ ! -f "$target" ]; then
	echo "${PROGNAME}: *ERROR* The target executable does not exist:"
        echo "${PROGNAME}:"
        echo "${PROGNAME}:    $target"
        echo "${PROGNAME}:"
	echo "${PROGNAME}: Please make sure you have checked out the 'tools' module"
	echo "${PROGNAME}: and make sure it is up to date."
        exit 2
    fi

    # Get busy yo!
    if [ "x$DEBUG" != "x" ]; then
	echo "${PROGNAME}: Executing: /bin/sh $target $@"
    fi
    if [ "x$TRACE" = "x" ]; then
	exec /bin/sh $target "$@"
    else
	exec /bin/sh -x $target "$@"
    fi
}

# Lets get ready to rumble!
main "$@"
