#!/bin/sh

# build-hpux.sh for mod_jk.so
#
# Usage: # sh build-hpux.sh
# for hpux 11 using gcc
# Note: See README-hpux for details.  This builds mod_jk without JNI support.
#
# Mike Braden

# Update the following according to your installation

APACHE_HOME=/usr/local/apache
JAVA_HOME=/opt/java1.3

#### End of configuration - do not change below

if [ -f $APACHE_HOME/bin/apxs ] ; then
   APXS=$APACHE_HOME/bin/apxs
else
   echo Error: Unable to locate apxs.
   echo Verify that APACHE_HOME is set correctly in this script.
   exit 1
fi
if [ ! -d $JAVA_HOME/include/hp-ux ] ; then
   echo Error: Unable to locate Java libraries.
   echo Verify that JAVA_HOME is set correctly in this script.
   exit 1
fi

JAVA_INCLUDE="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/hp-ux"

INCLUDE="-I../common $JAVA_INCLUDE"

#SRC="../common/jk_ajp12_worker.c ../common/jk_ajp13.c ../common/jk_ajp13_worker.c ../common/jk_connect.c ../common/jk_lb_worker.c ../common/jk_map.c ../common/jk_msg_buff.c ../common/jk_nwmain.c ../common/jk_pool.c ../common/jk_sockbuf.c ../common/jk_uri_worker_map.c ../common/jk_util.c ../common/jk_worker.c mod_jk.c"

SRC="../common/*.c mod_jk.c"

# Run APXS to compile the mod_jk module and its components
echo Building mod_jk
$APXS -o mod_jk.so $INCLUDE -c $SRC

# Check to see if the last command completed
if [ $? -ne 0 ] ; then
  echo Error with apxs
  exit 1
fi

echo mod_jk build complete.

#
# Clean up
#
rm jk_*.o
rm mod_jk.o

echo Configuring apache...

# Use apxs to add the correct lines to httpd.conf
# Since our auto-config does this in the include
# file (mod_jk-conf-auto), we'll add them as
# commented statements for change later if
# we decide not to use the auto-conf.
#
#$APXS -i -a mod_jk.so
$APXS -i -A mod_jk.so

# Check to see if the last command completed
if [ $? -ne 0 ] ; then
  echo Error using apxs to add configuration to httpd.conf
  exit 1
fi

# Steps to complete install
cat<<END

Build and configuration of mod_jk is complete.

To finish the installation, edit your apache/conf/httpd.conf file and
add the following line to the end of the file:
(Note: Change TOMCAT_HOME to the value of $TOMCAT_HOME)

Include TOMCAT_HOME/conf/jk/mod_jk.conf-auto

Example (/usr/local/apache/conf/httpd.conf):

Include /usr/local/jakarta-tomcat-3.3/conf/jk/mod_jk.conf-auto

Next copy TOMCAT_HOME/conf/jk/workers.properties.unix to
TOMCAT_HOME/conf/jk/workers.properties

Finally, add the apache auto-config setting to Tomcat.
See the release notes for Tomcat 3.3 for information on enabling
the auto-configure script in section 2, Tomcat Configuration:

"To turn these on, add the following modules after the
  <AutoWebApp ... /> module in the server.xml file:

  Apache configs:  <ApacheConfig />"

Example (TOMCAT_HOME/conf/serverl.xml):

        <AutoWebApp dir="webapps" host="DEFAULT" />

        <ApacheConfig />

For more information, see the mod_jk-howto located in the docs dir
of TOMCAT. (doc/mod_jk-howto.html)
END
