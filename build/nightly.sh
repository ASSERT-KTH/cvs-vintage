#!/bin/sh

echo "This script is not for the faint of heart!"
echo "You have 10 seconds to hit ctrl-c now"
echo "if you don't know what you are doing"
sleep 10

echo "Runbox update started: "
date

TURBINE="../../jakarta-turbine"
TORQUE="../../jakarta-turbine-torque"
SCARAB=".."
DIR=`pwd`
CVSUPDATE=1
MYSQL=/usr/local/mysql/bin
DEPBUILD=0

## Environment variables
JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Home"
export JAVA_HOME
ANT_HOME="/usr/local/ant"
export ANT_HOME
PATH=${PATH}:/usr/local/bin:${ANT_HOME}/bin:${MYSQL}
export PATH
echo "Path: $PATH"

## Kill Catalina
cd ${DIR}
if [ -x ${SCARAB}/target/bin/catalina.sh ] ; then
    echo "Killing Catalina..."
    ./${SCARAB}/target/bin/catalina.sh stop
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
cd ${DIR}
echo "Removing Torque and Turbine from ${SCARAB}/lib..."
rm -rf ${SCARAB}/lib/turbine*.jar
rm -rf ${SCARAB}/lib/torque*.zip

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

if [ ${DEPBUILD} -gt 0 ] ; then
# remove after the cvs update
cd ${DIR}
echo "Removing Torque and Turbine from ${SCARAB}/lib again..."
rm -rf ${SCARAB}/lib/turbine*.jar
rm -rf ${SCARAB}/lib/torque*.zip
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

echo "Recreating the database..."
cd ${DIR}; cd ${SCARAB}/src/sql
./create-mysql-database.sh

## Start Tomcat running...
cd ${DIR}
if [ -d ${SCARAB}/target/bin ] ; then
    echo "Starting Tomcat..."
    cd ${SCARAB}/target
    ./bin/startup.sh
fi

echo "Runbox update finished:"
date
