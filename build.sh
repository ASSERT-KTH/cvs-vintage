#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  This is the main entry point for the build system.                      ##
##                                                                          ##
##  Users should be sure to execute this file rather than 'ant' to ensure   ##
##  the correct version is being used with the correct configuration.       ##
##                                                                          ##
### ====================================================================== ###

# $Id: build.sh,v 1.7 2001/09/04 05:08:08 user57 Exp $

PROGNAME=`basename $0`
DIRNAME=`dirname $0`
GREP="grep"
ROOT="/"

# Ignore user's ANT_HOME if it is set
ANT_HOME=""

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

# the jaxp parser to use
if [ "x$JAXP" = "x" ]; then
    # Default to crimson
    JAXP="crimson"
fi

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
	    echo $ANT_HOME
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
    ANT_HOME=`search $ANT_SEARCH_PATH`

    # try looking up to root
    if [ "x$ANT_HOME" = "x" ]; then
	target="build"
	_cwd=`pwd`

	while [ "x$ANT_HOME" = "x" ] && [ "$cwd" != "$ROOT" ]; do
	    cd ..
	    cwd=`pwd`
	    ANT_HOME=`search $ANT_SEARCH_PATH`
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
    fi

    # make sure we have one
    ANT=$ANT_HOME/bin/ant
    if [ ! -x "$ANT" ]; then
	die "Ant file is not executable: $ANT"
    fi

    # specify the jaxp parser impls to use
    case "$JAXP" in
	crimson)
	    JAXP_DOM_FACTORY="org.apache.crimson.jaxp.DocumentBuilderFactoryImpl"
	    JAXP_SAX_FACTORY="org.apache.crimson.jaxp.SAXParserFactoryImpl"
	    ;;
	   
	xerces)
	    JAXP_DOM_FACTORY="org.apache.xerces.jaxp.DocumentBuilderFactoryImpl"
	    JAXP_SAX_FACTORY="org.apache.xerces.jaxp.SAXParserFactoryImpl"
	    ;;
    esac

    if [ "x$JAXP_DOM_FACTORY" != "x" ]; then
	ANT_OPTS="$ANT_OPTS -Djavax.xml.parsers.DocumentBuilderFactory=$JAXP_DOM_FACTORY"
    fi
    if [ "x$JAXP_SAX_FACTORY" != "x" ]; then
	ANT_OPTS="$ANT_OPTS -Djavax.xml.parsers.SAXParserFactory=$JAXP_SAX_FACTORY"
    fi

    # change to the directory where the script lives so users are not forced
    # to be in the same directory as build.xml
    cd $DIRNAME

    export ANT ANT_HOME ANT_OPTS
    exec $ANT $ANT_OPTIONS "$@"
}

##
## Bootstrap
##

main "$@"
