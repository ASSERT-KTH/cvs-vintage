#!/bin/sh

# A small wrapper script around the nightly.sh script
# that checks the output for status and mails it to
# a specific email address.
#
# $Id: nightly-cron.sh,v 1.3 2002/11/18 18:22:37 jon Exp $

LOGFILE=/tmp/nightly.log
LIST='dev@scarab.tigris.org'
#LIST='jon@whichever.com'

rm -rf $LOGFILE

./nightly.sh > $LOGFILE 2>&1

# find results from the tests
STAT=$(sed -n '/^.*Tests run:[ 0-9,]*/s///p;/BUILD FAILED/s//&/p;tX;d;:X;q' $LOGFILE)

case $STAT in
    *0*0|"") STAT=OK ;;
esac

mail -s "Scarab Nightly Runbox Build [$STAT]" $LIST < $LOGFILE
