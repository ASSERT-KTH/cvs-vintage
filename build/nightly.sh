#!/bin/sh

echo "This script is not for the faint of heart!"
echo "You have 10 seconds to hit ctrl-c now"
echo "if you don't know what you are doing"
sleep 10

Bar()
{
echo "---------------------------------"
}

StopTomcat()
{
Bar
cd ${DIR}
if [ -d ${SCARAB}/target/bin ] ; then
  echo "Stopping Tomcat..."
  cd ${SCARAB}/target
  ./bin/catalina.sh stop
fi
sleep 5
Bar
}

StartTomcat()
{
Bar
cd ${DIR}
if [ -d ${SCARAB}/target/bin ] ; then
  echo "Starting Tomcat..."
  cd ${SCARAB}/target
  ./bin/catalina.sh start
fi
sleep 5
Bar
}

echo "Runbox update started: "
date

TURBINE="../../jakarta-turbine-3"
TORQUE="../../jakarta-turbine-torque"
SCARAB=".."

CHECKSTYLE=1
CVSUPDATE=1
DEPBUILD=0

case "$OSTYPE" in
  darwin*)
    DARWIN=1
  ;;
  *)
    DARWIN=0
  ;;
esac

DIR=`pwd`
MYSQL=/usr/local/mysql/bin
WGET=/usr/bin/wget

## Environment variables
if [ -z ${JAVA_HOME} ] ; then
  if [ ${DARWIN} -gt 0 ] ; then
    JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Home"
  elif [ ${OSTYPE} = "linux" ] ; then
    JAVA_HOME="/usr/local/java"
  fi
  export JAVA_HOME
fi
echo "JAVA_HOME: ${JAVA_HOME}"

if [ -z ${ANT_HOME} ] ; then
  ANT_HOME="/usr/local/ant"
  export ANT_HOME
fi
PATH=${PATH}:/usr/local/bin:${ANT_HOME}/bin:${MYSQL}
export PATH
echo "Path: $PATH"

## Kill Catalina
cd ${DIR}
if [ -x ${SCARAB}/target/bin/catalina.sh ] ; then
  echo "Killing Catalina..."
  StopTomcat
else
  echo "Target directory doesn't exist. No need to kill Catalina..."
fi

## Clean up old builds
cd ${DIR}
if [ -d ${SCARAB}/target ] ; then
  echo "Removing Scarab ${SCARAB}/target directory..."
  cd ${DIR}; cd ${SCARAB}
  rm -r target
else
  echo "Scarab target directory does not exist..."    
fi

# remove so we don't have conflicts
if [ ${CVSUPDATE} -gt 0 ] ; then
  cd ${DIR}
  echo "Removing Torque and Turbine from ${SCARAB}/lib..."
  rm -rf ${SCARAB}/lib/turbine*.jar
  rm -rf ${SCARAB}/lib/torque*.zip
fi

if [ ${DEPBUILD} -gt 0 ] ; then
  cd ${DIR}
  if [ -d ${TURBINE}/bin ] ; then
    echo "Removing Turbine ${TURBINE}/bin directory..."
    cd ${DIR}; cd ${TURBINE}
    rm -r bin
  else
    echo "Turbine bin directory does not exist..."    
  fi
fi

if [ ${DEPBUILD} -gt 0 ] ; then
  cd ${DIR}
  if [ -d ${TORQUE}/bin ] ; then
    echo "Removing Torque ${TORQUE}/bin directory..."
    cd ${DIR}; cd ${TORQUE}
    rm -r bin
  else
    echo "Torque bin directory does not exist..."    
  fi
fi

## Update things from CVS
if [ ${CVSUPDATE} -gt 0 ] ; then
  if [ ${DEPBUILD} -gt 0 ] ; then
    echo "Updating Torque From CVS Start..."
    date
    cd ${DIR}; cd ${TORQUE}
    cvs update
    echo "Updating Torque From CVS Finish..."
    date

    echo "Updating Turbine From CVS Start..."
    date
    cd ${DIR}; cd ${TURBINE}
    cvs update
    echo "Updating Turbine From CVS Finish..."
    date
  fi
    echo "Updating Scarab From CVS Start..."
    date
    cd ${DIR}; cd ${SCARAB}
    cvs update
    echo "Updating Scarab From CVS Finish..."
    date
  else
    echo "Skipping CVS update..."
fi

if [ ${CVSUPDATE} -gt 0 ] ; then
  if [ ${DEPBUILD} -gt 0 ] ; then
    # remove after the cvs update
    cd ${DIR}
    echo "Removing Torque and Turbine from ${SCARAB}/lib again..."
    rm -rf ${SCARAB}/lib/turbine*.jar
    rm -rf ${SCARAB}/lib/torque*.zip
  fi
fi

## Build things now
if [ ${DEPBUILD} -gt 0 ] ; then
  echo "Building Torque Start..."
  cd ${DIR}; cd ${TORQUE}
  ant dist
  echo "Building Torque Finish..."

  echo "Building Turbine Start..."
  cd ${DIR}; cd ${TURBINE}/build
  ant jar
  echo "Building Turbine Finish..."
fi

echo "Building Scarab Start..."
cd ${DIR}; cd ${SCARAB}/build
if [ ${DEPBUILD} -gt 0 ] ; then
  ant upgrade-torque-turbine
fi
ant
echo "Building Scarab Finish..."

echo "Building Scarab Javadoc Start..."
cd ${DIR}; cd ${SCARAB}/build
ant javadocs
echo "Building Scarab Javadoc Finish..."

echo "Recreating the database..."
cd ${DIR}; cd ${SCARAB}/src/sql
./create-db.sh

Bar
echo "Running the test suite..."
cd ${DIR}; cd ${SCARAB}/build
ant -f run-tests.xml
Bar

if [ ${CHECKSTYLE} -gt 0 ] ; then
Bar
  echo "Running checkstyle-mail..."
  cd ${DIR}; cd ${SCARAB}/build
  ant checkstyle-mail
Bar
fi

echo "Recreating the database (to clear out the test data)..."
cd ${DIR}; cd ${SCARAB}/src/sql
./create-db.sh

StartTomcat

echo "Runbox update finished:"
date
