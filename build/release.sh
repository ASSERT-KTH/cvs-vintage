#!/bin/sh

# $Id: release.sh,v 1.2 2002/08/28 20:02:24 jon Exp $
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
#TAG=SCARAB_1_0_B10
#TAG=SCARAB_1_0_B9
VERSION=b10
VERSIONNEXT=b11

if [ -z ${TAG} ] ; then
    echo "This script is not for the faint of heart.";
    echo "Need to edit this script and specify a tag.";
    exit;
fi

CVSROOT=:pserver:${USER}@scarab.tigris.org:/cvs
export CVSROOT

echo "removing scarab from /tmp/scarab"
rm -r /tmp/scarab

cd /tmp
START_DIR=`pwd`

echo "startdir: ${START_DIR}" 
echo "current dir1: "; echo `pwd`

cvs co scarab

cd ${START_DIR}/scarab/build

echo "current dir2: "; echo `pwd`

echo "Doing new version replacement 1"
perl -pi -e "s/${VERSION}-dev/${VERSION}/" default.properties
cvs ci default.properties

cd ${START_DIR}
echo "current dir3: "; echo `pwd`

echo "tagging cvs"
cvs tag ${TAG} scarab

echo "updating cvs to the tag"
cvs up -r ${TAG} scarab

cd ${START_DIR}/scarab/build
echo "current dir4: "; echo `pwd`
echo "running ant package"
ant package

echo "Doing new version replacement 2"
perl -pi -e "s/${VERSION}/${VERSIONNEXT}-dev/" default.properties
# remove the tag
cvs up -A default.properties
cvs ci default.properties

echo "All done!"
