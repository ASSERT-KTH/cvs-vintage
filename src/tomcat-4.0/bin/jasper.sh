#!/bin/sh
# -----------------------------------------------------------------------------
# jasper.sh - Global Script Jasper
#
# Environment Variable Prequisites
#
# Environment Variable Prequisites:
#   JASPER_HOME (Optional)
#       May point at your Jasper "build" directory.
#       If not present, the current working directory is assumed.
#   JASPER_OPTS (Optional) 
#       Java runtime options
#   JAVA_HOME     
#       Must point at your Java Development Kit installation.
#
# $Id: jasper.sh,v 1.5 2001/05/15 21:38:07 jon Exp $
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ -z "$JASPER_HOME" ] ; then
  ## resolve links - $0 may be a link to  home
  PRG=$0
  progname=`basename $0`
  
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG="`dirname $PRG`/$link"
    fi
  done
  
  JASPER_HOME_1=`dirname "$PRG"`/..
  echo "Guessing JASPER_HOME from catalina.sh to ${JASPER_HOME_1}" 
    if [ -d ${JASPER_HOME_1}/conf ] ; then 
	JASPER_HOME=${JASPER_HOME_1}
	echo "Setting JASPER_HOME to $JASPER_HOME"
    fi
fi

if [ "$JASPER_OPTS" = "" ] ; then
  JASPER_OPTS=""
fi

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

# ----- Set Up The System Classpath -------------------------------------------

# FIXME CP=$JASPER_HOME/dummy
# FIXME below
CP=$CP:$JASPER_HOME/classes
for i in $JASPER_HOME/lib/*.jar $JASPER_HOME/jasper/*.jar ; do
  CP=$CP:$i
CP=$CP:$JASPER_HOME/common/lib/servlet.jar
done

# convert the existing path to windows
if [ "$OSTYPE" = "cygwin32" ] || [ "$OSTYPE" = "cygwin" ] ; then
   CP=`cygpath --path --windows "$CP"`
   JASPER_HOME=`cygpath --path --windows "$JASPER_HOME"`
fi

echo Using CLASSPATH: $CP


# ----- Execute The Requested Command -----------------------------------------

if [ "$1" = "jspc" ] ; then

  shift
  java $JASPER_OPTS -classpath $CP \
   -Djasper.home=$JASPER_HOME \
   org.apache.jasper.JspC "$@"

else

  echo "Usage: jasper.sh ( jspc )"
  echo "Commands:"
  echo   jspc - Run the jasper offline JSP compiler
  exit 1

fi
