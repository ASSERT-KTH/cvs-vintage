#! /bin/sh
#

# $Id: build.sh,v 1.7 2002/11/23 22:04:38 kataka Exp $

#
 
# Always use the ant that comes with ArgoUML
ANT_HOME=../tools/ant-1.4.1

echo ANT_HOME is: $ANT_HOME
echo
echo Starting Ant...
echo

$ANT_HOME/bin/ant $*

exit
