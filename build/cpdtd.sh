#!/bin/sh

#
# This is a hacky little script that attempts to get around a bug in JDK 1.3
# JVM's on some Unix platforms where .dtd files cannot be found if they are 
# in a .jar file. So, what this script does is it checks to see if you have 
# Turbine checked out. If so, it will use the CVS version of the files in 
# Turbine, otherwise it uses the locally checked in files in Scarab.
#
# We could do this in Ant, but getting all the conditional targets right 
# is more of a pain in the rear than just doing a little shell script.
#
# $Id: cpdtd.sh,v 1.1 2001/04/06 01:59:16 jon Exp $
#

TURBINE=../../jakarta-turbine

if [ -d ${TURBINE} ] && [ ! -e ../src/dtd/intake.dtd ] ; then

    echo "intake.dtd is missing from Scarab...fixing"
    cp ${TURBINE}/src/dtd/intake.dtd \
       ../src/dtd/intake.dtd

elif [ -d ${TURBINE} ] && [ ${TURBINE}/src/dtd/intake.dtd -nt ../src/dtd/intake.dtd ] ; then

    echo "Updating intake.dtd from Turbine CVS..."
    cp ${TURBINE}/src/dtd/intake.dtd \
       ../src/dtd/intake.dtd
       
fi

if [ -d ${TURBINE} ] && [ ! -e ../src/dtd/database.dtd ] ; then

    echo "database.dtd is missing from Scarab...fixing"
    cp ${TURBINE}/src/dtd/database.dtd \
       ../src/dtd/database.dtd

elif [ -d ${TURBINE} ] && [ ${TURBINE}/src/dtd/database.dtd -nt ../src/dtd/database.dtd ] ; then

    echo "Updating database.dtd from Turbine CVS..."
    cp ${TURBINE}/src/dtd/database.dtd \
       ../src/dtd/database.dtd

fi

mkdir -p ../target/webapps/scarab/WEB-INF/classes/org/apache/turbine/services/intake/transform
cp ../src/dtd/intake.dtd \
   ../target/webapps/scarab/WEB-INF/classes/org/apache/turbine/services/intake/transform

mkdir -p ./org/apache/turbine/torque/engine/database/transform
cp ../src/dtd/database.dtd \
   ./org/apache/turbine/torque/engine/database/transform/database.dtd
