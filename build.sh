#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  This is the main entry point for the build system.                      ##
##                                                                          ##
##  Users should be sure to execute this file rather than 'ant' to ensure   ##
##  the correct version is being used with the correct configuration.       ##
##                                                                          ##
### ====================================================================== ###

# $Id: build.sh,v 1.4 2001/08/27 04:47:22 user57 Exp $

PROGNAME=`basename $0`
DIRNAME=`dirname $0`
GREP="grep"
ROOT="/"

# the default search path for ant
ANT_SEARCH_PATH="\
    tools
    tools/ant \
    tools/apache/ant \
    ant"

# the default build file name
ANT_BUILD_FILE="build.xml"

# the default arguments
ANT_OPTIONS="-find $ANT_BUILD_FILE"

# don't check versions (too slow)
ANT_VERSION=""

#
# Helper to complain.
#
die() {
    echo "${PROGNAME}: $*"
    exit 1
}

#
# Helper to source a file if it exists.
#
maybe_source() {
    for file in $*; do
	if [ -f "$file" ]; then
	    . $file
	fi
    done
}

search() {
    search="$*"
    for d in $search; do
	ANT_HOME="`pwd`/$d"
	ANT="$ANT_HOME/bin/ant"
	if [ -x "$ANT" ]; then
	    # found one
	    echo $ANT
	    break
	fi
    done
}

#
# Main function.
#
main() {
    # if there is a build config file. then source it
    maybe_source "$DIRNAME/build.conf" "$HOME/.build.conf"

    # try the search path
    ANT=`search $ANT_SEARCH_PATH`
    target="build"
    _cwd=`pwd`

    while [ "x$ANT" = "x" ] && [ "$cwd" != "$ROOT" ]; do
	cd ..
	cwd=`pwd`
	ANT=`search $ANT_SEARCH_PATH`
    done

    # make sure we get back
    cd $_cwd

    if [ "$cwd" != "$ROOT" ]; then
	found="true"
    fi

    # complain if we did not find anything
    if [ "$found" != "true" ]; then
	die "Could not locate Ant; check \$ANT or \$ANT_HOME."
    fi

    # make sure we have one
    if [ ! -x "$ANT" ]; then
	die "Ant file is not executable: $ANT"
    fi

    # perhaps check the version
    if [ "x$ANT_VERSION" != "x" ] && [ "x$ANT_VERSION_CHECK" != "x" ]; then
	result="`$ANT -version 2>&1 | $GREP $ANT_VERSION`x"
	if [ "$result" = "x" ]; then
	    die "Ant version $ANT_VERSION is required to build."
	fi
    fi

    # change to the directory where the script lives so folks do not have
    # to be in the same dir to run the build without specifying the build
    # file. 
    cd $DIRNAME

    export ANT ANT_HOME
    exec $ANT $ANT_OPTIONS "$@"
}

##
## Bootstrap
##

main "$@"
