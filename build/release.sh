#!/bin/sh

# $Id: release.sh,v 1.1 2002/08/27 21:10:13 jon Exp $
#
# Procedure for release:
#
# 1) Edit the TAG line below to specify the cvs tag.
#    ie: SCARAB_1_0_B9
# 2) Edit the VERSION/VERSIONNEXT to specify the current
#    version in the file and the new version.
#    ie: b9 / b10
# 3) Run this script.
#
# Other files to modify:
# www/project_status.html (update the project status)
# www/index.html (update the version number)
# www/changes.html (start logging new changes)
#
# Don't forget to upload the files to the website.
# Don't forget to update the freshmeat.net notice.
#

TAG=
#TAG=SCARAB_1_0_B9
VERSION=b9
VERSIONNEXT=b10

if [ -z ${TAG} ] ; then
    echo "This script is not for the faint of heart.";
    echo "Need to edit this script and specify a tag.";
    exit;
fi

CVSROOT=:pserver:${USER}@scarab.tigris.org:/cvs
export CVSROOT

cd /tmp

cvs co scarab

cd scarab/build
perl -pi -e "s/${VERSION}-dev/${VERSION}/" default.properties
cvs ci default.properties

cd ../../

cvs tag ${TAG} scarab

cvs up -r ${TAG} scarab

cd scarab/build
ant package

perl -pi -e "s/${VERSION}/${VERSIONNEXT}-dev/" default.properties
cvs ci default.properties
