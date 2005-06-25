#! /bin/sh
#

# $Id: build.sh,v 1.8 2005/06/25 23:35:42 linus Exp $

#
 
# Always use the ant that comes with ArgoUML
ANT_HOME=../tools/ant-1.6.2

echo ANT_HOME is: $ANT_HOME
echo
echo Starting Ant...
echo

$ANT_HOME/bin/ant $*

exit
