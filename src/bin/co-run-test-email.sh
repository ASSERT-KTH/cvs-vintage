#!/bin/sh

echo Put this file where you want the clean checkout of jboss to occur,
echo and set the email addresses as appropriate.  Then comment out the 
echo exit line and run...

exit

#the module to check out

MODULE=jboss-head

#remove previous version

rm -rf $MODULE

cvs -z9 -d:pserver:anonymous@cvs.jboss.sourceforge.net:/cvsroot/jboss co $MODULE

cd $MODULE/build/


./build.sh main tests\
 -Dtest.report.email.tolist=somewhere@your.isp.net\
 -Dtest.report.email.from=you@your.isp.net\
 -Dtest.report.email.mailhost=localhost



