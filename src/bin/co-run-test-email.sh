#!/bin/sh

#This script removes any current jboss checkout, does a jboss checkout,
#builds and starts jboss, runs the testsuite, and mails the results.
#Specify the email addresses lower down.

echo Put this file where you want the clean checkout of jboss to occur,
echo and set the email addresses as appropriate.  Then comment out the 
echo exit line and run...

exit

TO_EMAIL=noone@nowhere.com
FROM_EMAIL=noone@nowhere.com
EMAIL_SERVER=localhost

#the module to check out

MODULE=jboss-head

#remove previous version

rm -rf $MODULE

cvs -z9 -d:pserver:anonymous@cvs.jboss.sourceforge.net:/cvsroot/jboss co $MODULE

cd $MODULE/build/

./build.sh run-nightly \
 -Dtest.skipupdate=true \
 -Drun-nightly-sleep=3\
 -Drun.nightly.email.tolist=$TO_EMAIL \
 -Drun.nightly.email.from=$FROM_EMAIL \
 -Drun.nightly.email.mailhost=$EMAIL_SERVER

