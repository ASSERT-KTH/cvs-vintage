#!/bin/sh

echo "Runbox update started: "
date

TURBINE="../../jakarta-turbine"
SCARAB=".."
DIR=`pwd`
CVSUPDATE=1
MYSQL=/usr/local/mysql/bin

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

cd ${DIR}
echo "Removing Torque and Turbine from ${SCARAB}/lib..."
rm -rf ${SCARAB}/lib/turbine*.jar
rm -rf ${SCARAB}/lib/torque*.zip

cd ${DIR}
if [ -d ${TURBINE}/bin ] ; then
    echo "Removing Turbine ${TURBINE}/bin directory..."
    cd ${DIR}; cd ${TURBINE}
    rm -r bin
else
    echo "Turbine bin directory does not exist..."    
fi

## Update things from CVS
if [ ${CVSUPDATE} -gt 0 ] ; then
    echo "Updating Turbine From CVS..."
    cd ${DIR}; cd ${TURBINE}
    cvs update
    
    echo "Updating Scarab From CVS..."
    cd ${DIR}; cd ${SCARAB}
    cvs update
else
    echo "Skipping CVS update..."
fi

## Build things now
echo "Building Turbine Start..."
cd ${DIR}; cd ${TURBINE}/build
ant jar
echo "Building Turbine Finish..."

echo "Building Scarab Start..."
cd ${DIR}; cd ${SCARAB}/build
./build.sh
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
