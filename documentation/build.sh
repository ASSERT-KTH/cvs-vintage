#! /bin/sh
#
# $Id: build.sh,v 1.6 2002/11/17 09:08:18 linus Exp $
#
 
# Always use the ant that comes with ArgoUML
ANT_HOME=../tools/ant-1.4.1

echo ANT_HOME is: $ANT_HOME
echo
echo Starting Ant...
echo

$ANT_HOME/bin/ant $*

exit
