#! /bin/sh
# $Id: build2.sh,v 1.1 2006/03/08 16:34:02 tfmorris Exp $
#
 
# +-------------------------------------------------------------------------+
# | Verify and Set Required Environment Variables                           |
# +-------------------------------------------------------------------------+
if [ "$JAVA_HOME" = "" ] ; then
	echo "***************************************************************"
	echo "  ERROR: JAVA_HOME environment variable not found."
	echo ""
	echo "  Please set JAVA_HOME to the Java JDK installation directory."
	echo "***************************************************************"
	exit 1
fi

#
# build.sh always calls the version of ant distributed with ArgoUML
#
ANT_HOME=../argouml-core-tools/ant-1.6.2

echo ANT_HOME is: $ANT_HOME
echo
echo Starting Ant...
echo

$ANT_HOME/bin/ant -Doverride.build.properties=../argouml/eclipse-ant-build.properties -Dargo.root.dir=`pwd`/.. $*

#exit
