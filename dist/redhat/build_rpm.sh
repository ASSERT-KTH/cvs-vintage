#!/bin/sh

VERSION=0.10.0

# change directory

cd ../../

# unzip unix binary package
unzip columba-unix-$VERSION-bin.zip

# replace run.sh with rpm-specific columba script
cp dist/redhat/columba.sh columba-unix-$VERSION-bin/run.sh

# make run.sh executable
chmod a+x columba-unix-$VERSION-bin/run.sh

mv columba-unix-$VERSION-bin columba-$VERSION

# create tar-archive in /usr/src/RPM/SOURCES/columba-$VERSION-bin.tar.gz
tar cf /usr/src/redhat/SOURCES/columba-unix-$VERSION-bin.tar.gz columba-$VERSION

# create /usr/src/redhat/SRPMS/columba-$VERSION-0.src.rpm 
rpmbuild -bs dist/redhat/columba.spec

# create /usr/src/redhat/RPMS/noarch/columba-$VERSION-0.noarch.rpm
rpmbuild --rebuild -v --target noarch /usr/src/redhat/SRPMS/columba-$VERSION-0.src.rpm

# move rpm-file to directory .
mv  /usr/src/redhat/RPMS/noarch/columba-$VERSION-0.noarch.rpm .

# remove obsolete rpm-file
rm -f /usr/src/redhat/RPMS/noarch/columba-$VERSION-0.noarch.rpm 

# remove obsolete directory
rm -rf columba-$VERSION

