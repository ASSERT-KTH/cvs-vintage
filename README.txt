$Id: README.txt,v 1.56 2003/05/01 00:22:12 jon Exp $

Welcome to Scarab!

The installation and configuration of Scarab is intended to be as simple as 
possible. This document outlines the directory structure of the distribution 
(currently it mirrors the CVS tree), the requirements for running Scarab,
the instructions for building and running an installation as well as 
requirements for setting up the database.

If you are doing development work with Scarab, please make sure to read 
this link: http://scarab.tigris.org/development.html

We appreciate your deciding to try Scarab out and appreciate constructive 
feedback on your success (or failure...we hope not!) in getting the 
system running.


-------------------------------------------------------------------------
| R E Q U I R E M E N T S                                               |
-------------------------------------------------------------------------

SDK 1.3 or higher        --> <http://java.sun.com/>
Make sure that you download the SDK and not the JRE!
(Note: On some operating systems such as OS X, the SDK comes with it.)

Ant 1.4 or higher        --> <http://jakarta.apache.org/ant/>
The version of torque used with scarab must be compiled with Ant 1.4.x

Tomcat 4.0.4 or higher   --> <http://jakarta.apache.org/tomcat/>
                             (Note: Tomcat 4.1.x is included with Scarab.)

MySQL 3.23.x/4.x         --> <http://www.mysql.org/>
                           OR
Postgresql 7.3.x         --> <http://www.postgresql.org/>

NOTE: More detailed instructions for setting up the database on
      different database vendors is available on our website.

      **You should read it BEFORE building Scarab.**

      <http://scarab.tigris.org/project_docs.html>

NOTE: If you want to use the faster/newer JDBC driver with MySQL, you can
      download it from the MySQL website and use them instead. We do not
      distribute it because it is GPL. To use it, just copy the .jar
      file into your scarab/lib directory and put this in your
      build.properties file: scarab.jdbc.driver.jar=mysql-connector*.jar
      
      <http://www.mysql.com/downloads/api-jdbc-dev.html>

NOTE: Scarab requires Jikes 1.18 or higher to compile. Please do not try
      with Jikes 1.17 as it is buggy.

NOTE: If you are using an existing Tomcat 4.1.x installation, you MUST
      *move* the common/endorsed/xercesImpl.jar to the server/lib
      directory. Please note that if you have any existing web
      applications that depend on Xerces 2, you will need to copy that
      .jar file into their WEB-INF/lib in order to make things work
      properly after the move.

You must have the JAVA_HOME environment variable properly set to be the 
location of your SDK installation directory. On MacOSX, this path is:
/System/Library/Frameworks/JavaVM.framework/Home

You must have Ant installed and ANT_HOME defined in your environment as
well as ANT_HOME/bin in your PATH.

The database is assumed to be installed and running with appropriately
configured access control setup (see below for more detail). You must
have the database binaries in your PATH (ie: $MYSQL_HOME/bin). 

    With sh/zsh/bash:
        export ANT_HOME=/path/to/ant-install
        export MYSQL_HOME=/path/to/mysql-install
        export JAVA_HOME=/path/to/jdk-install
        export PATH=${PATH}:${ANT_HOME}/bin:${MYSQL_HOME}/bin:${JAVA_HOME}/bin

    With csh/tcsh:
        setenv ANT_HOME /path/to/ant-install
        setenv MYSQL_HOME /path/to/mysql-install
        setenv JAVA_HOME /path/to/jdk-install
        setenv PATH ${PATH}:${ANT_HOME}/bin:${MYSQL_HOME}/bin:${JAVA_HOME}/bin

    Note: To make these settings 'sticky', put them into the appropriate
          .rc file for your shell. For example, if you use tcsh, put the
          lines above into your ~/.tcshrc

With the Scarab communities help, we will be supporting a wide range of
databases in the released version of Scarab, however, the CollabNet 
developers are currently doing development primarily on MySQL and thus
do not guarantee that Scarab will work on other databases.

All of the necessary .jar files for building and running Scarab are
included in the /lib directory and the build system is setup to include
these into your classpath for you. Please do not add any jar files to
your CLASSPATH as it may cause compile errors.

If you already have an existing webserver or service running on ports
8080 and 8005, and you are using Scarab's version of Tomcat, you will
need to change the port number to another unused port number by defining
the scarab.tomcat.http.port and scarab.tomcat.shutdown.port properties
in your build.properties (please see the "Settings" instructions below
for how to create a build.properties file). Once you have done this, you
will need to rebuild the sandbox.

By default, the web applications WEB-INF directory needs to have
permissions set so that the userid which the JVM is running under can
write into that directory.

NOTE: One should use the copy of Xerces 1.x that is included with
Scarab and make sure that no other copies of Xerces (especially
Xerces 2.x) are in your JAVA_HOME, ANT_HOME or your CLASSPATH.
Otherwise, you may get build errors.


-------------------------------------------------------------------------
| D I R E C T O R Y  S T R U C T U R E                                  |
-------------------------------------------------------------------------

Here is a description of the Scarab directory tree:

scarab/
    /build      <-- This is where the scripts are to build the target sandbox
                    and compile the source code for Scarab.
    /lib        <-- These are the .jar files used by Scarab.
    /src        <-- This where the source files are stored.
    /target     <-- This is the output directory where the sandbox will be 
                    created. It doesn't exist until you run the build script.
    /www        <-- This is where the website is stored.

Within the /src directory are a number of sub directories...

/src
    /conf       <-- Various configuration files for Scarab.
                    TurbineResources.properties and Scarab.properties
                    live here.
    /dtd        <-- Scarab, Intake and Torque DTD's.
    /html       <-- Files which show up within the webapp directory.
    /i18n       <-- Location of internationalized files.
    /images     <-- Copied to the webapp/images directory.
    /java       <-- The Java source code for Scarab.
    /scripts    <-- Helper shell scripts.
    /sql        <-- SQL files for defining the database.
    /templates  <-- Velocity templates for the HTML and Email.
    /test       <-- Test suite code.
    /tomcat-4.1 <-- A minimal copy of Tomcat 4.1.x for use with the Scarab
                    sandbox.


-------------------------------------------------------------------------
| S E T T I N G S                                                       |
-------------------------------------------------------------------------

The Scarab build process depends on having a few properties which are
defined in the build/default.properties. The settings in the
build/default.properties are fairly well documented within the file.
These properties should be set accordingly *before* you build Scarab.

If you would like to change the settings in the default.properties there
is no need to edit the default.properties file, you can override the
property settings by creating one or more of the following files and
placing your own property settings in there:

    ~/scarab.build.properties
    ~/build.properties
    scarab/build/build.properties

The first property which is found in the order with which the files are
loaded becomes the property setting which is used by the Ant build
system.

Chances are that you are going to have to define your own database and
mail server properties as well as a few other properties. Please look in
the scarab/build/default.properties for a list of things that you can
define.

NOTE: The ~ character represents your user account home directory.


-------------------------------------------------------------------------
| S E T T I N G  T H E  M A I L S E R V E R                             |
-------------------------------------------------------------------------

In order to use Scarab, you need to first set the relay outgoing mail
server so that email can be sent from Scarab. This is important for many
different aspects of Scarab, such as the confirmation email sent when a
user registers with the system. By default, the mail server is defined
as "localhost". That means that you need to have an SMTP server running
on the same box as Scarab. It is possible to modify this value by
following the directions above and setting the property
"scarab.system.mail.host" and then rebuilding Scarab.

Behind the scenes, in the build system, the "system.mail.host" property
is located in the src/conf/TurbineResources.properties file and gets
replaced with setting for "scarab.system.mail.host" in your local copy
of the TurbineResources.properties file (which is located in the target)
directory.

NOTE: If you modify this value after you have build the sandbox
      (instructions below), then you will need to re-run the ant
      script and restart the servlet engine in order for the changes to
      take effect.


-------------------------------------------------------------------------
| S E T T I N G  T H E  U P L O A D  D I R E C T O R Y                  |
-------------------------------------------------------------------------

When users attach files to an issue, by default, the file is saved to
disk in the WEB-INF/attachments directory. This may or may not be an
optimal place to store attachments potentially because of disk size
issues. Therefore, one has one of two options to solve this problem.

#1. On Unix, one can move the WEB-INF/attachments directory to another
location and then create a symlink from that location to the
WEB-INF/attachments.

#2. On Unix and other platforms (Win32), one can set the
scarab.attachments.path property to point at another directory. By
default, this path is relative to the webapp directory and is set to
"WEB-INF/attachments". It is also possible to define an absolute path
such as "/bigdisk/scarab/attachments".

It is also recommended to set the scarab.file.upload.path property as
well. This property is used as a temporary location for the uploaded
data during the upload process. By default it is "WEB-INF".

NOTE: Please see the documentation above for instructions on how to set
      properties and rebuild Scarab.


-------------------------------------------------------------------------
| S E T T I N G  T H E  I N D E X E S  D I R E C T O R Y                |
-------------------------------------------------------------------------

Scarab uses Lucene to create searchable indexes for the issue data.
Lucene needs to be able to store its indexes somewhere on disk. If the
disk is low on space, it is recommended to put the indexes on another
disk. This can be done by setting the scarab.lucene.index.path property.
This path defaults to "WEB-INF/index" and can be defined as either a
relative path or an absolute path.

NOTE: Please see the documentation above for instructions on how to set
      properties and rebuild Scarab.


-------------------------------------------------------------------------
| B U I L D I N G  T H E  S A N D B O X                                 |
-------------------------------------------------------------------------

The Scarab sandbox contains everything you need in order to get started
with Scarab. It includes a stripped down version of the Java Servlet
Engine (Tomcat) pre-configured to run Scarab.

To build the sandbox on your machine, you simply need to type the
following:

        cd build
        ant

This will create a directory in the scarab directory called "target".
You can safely remove this directory at any point as the source files
for creating this directory are in the /lib and /src directory. If you
edit any of the files in the /lib or /src directory, then you should
simply re-run the ant script and it will deal with copying and compiling
the changed files.

NOTE: Make sure that your TOMCAT_HOME environment variable is defined
      correctly. If you are using the Tomcat that comes with Scarab, you
      can safely undefine this value and follow the directions below for
      running the sandbox.

NOTE: If you already have an existing Tomcat installation and prefer
      to run Scarab from there, first build Scarab and then copy the 
      target/webapps/scarab directory into your own Tomcat installation.

NOTE: If you already have an existing webserver running on port 8080,
      and you are using Scarab's version of Tomcat, you will need to
      change the port number to another unused port number by defining
      the scarab.tomcat.http.port and scarab.tomcat.shutdown.port
      properties in your build.properties (please see the "Settings"
      instructions below for how to create a build.properties file).
      Once you have done this, you will need to rebuild the sandbox.
      
NOTE: There may be problems building and running Scarab with Tomcat 3.x.
      We have not done testing with this version of Tomcat.

NOTE: You should use the copy of Xerces 1.x that is included with Scarab
      and make sure that no other copies of Xerces (especially Xerces
      2.x) are in your JAVA_HOME, ANT_HOME or your CLASSPATH. Otherwise,
      you may get build errors.


-------------------------------------------------------------------------
| I N S T A L L I N G  T H E  D A T A B A S E                           |
-------------------------------------------------------------------------

The process of building Scarab creates the .sql file that is used to
describe the Scarab database schema. To install the database schema's,
you will need to install the database on your system and put the path to
the database binaries into your PATH environment variable.

Simply put, the idea is that the database creation scripts and the Java
database driver (JDBC) need the ability to connect to the database. In
order to do this, the code needs to be given a host machine, database
name, username and password.

By default, the scripts assume a database called 'scarab' and no
username/password to connect to the database on localhost. For example,
if you have MySQL up and running with no username/password for localhost
access, you can simply execute the following:

    cd src/sql
    ./create-db.sh               <-- Unix
    create-mysql-database.bat    <-- Win32

If you need to specify a host/username/password/databasename, you will
need to specify command line arguments to the create-db.sh script (Unix)
or edit the create-mysql-database.bat script (Win32) in order to specify
these settings to the MySQL client. For example:

    cd src/sql
    ./create-db.sh -u jon --password -P 3306 -h mysql.server.com

Also make sure to define your own scarab.database.* properties in your
local build.properties (see above for the explanation about how to use
build.properties) based on what is in the
scarab/build/default.properties. Once you have done this, you will need
to build the sandbox again in order to generate the right configuration
files based on this information.

NOTE: If you would like to only load the required database data and not
      the sample/default data, you can do so by passing the -e flag to
      the ./create-db.sh script or editing the .bat script to not load
      the *default*.sql and *sample*.sql files.

NOTE: More detailed instructions for setting up the database on
      different database vendors is available on our website.

      <http://scarab.tigris.org/project_docs.html>

NOTE: The create scripts will attempt to first drop a database called
      "scarab" and then re-create it. If you execute this create-*
      script, all of your previous data in that specific database will
      be lost without warning!

NOTE: If you get a 'Server configuration denies access to data source'
      or 'access denied' or 'Invalid authorization' or
      'java.lang.NullPointerException: Connection object was null.'
      error from MySQL in the log files, please read the MySQL
      documentation on how to fix this error. We will not provide
      support for this since it is really a MySQL configuration issue.

      Hint: On some operating systems, there seems to be a weird
      interation between the JVM, DNS resolution and the MySQL driver
      where a JDBC url pointing to 'localhost' will resolve as
      'localhost.localdomain' and will prevent the connection to MySQL
      from authenticating correctly because most people configure MySQL
      for 'localhost'. One way to get around this is to use IP addresses
      in both the MySQL ACL as well as in the JDBC url.

      At least one person has reported that using '127.0.0.1' instead of 'localhost'
      resolved a 'Server configuration denies access to data source' connection issue.

      In order to setup the right permissions in MySQL, you may wish to
      try executing this command on a Unix command line (it has been
      reported to work for one person):

        echo "GRANT ALL ON scarab.* to ''@localhost" | mysql mysql
      
      Here are a couple of links to also help you solve the permissions
      problem:
      
      <http://www.mysql.com/documentation/mysql/bychapter/
       manual_MySQL_Database_Administration.html#Access_denied>

      <http://sourceforge.net/docman/display_doc.php?docid=8968&group_id=15923>

NOTE: Sometimes it may be useful to use "ant create-db" rather than "create-db.sh"
      to diagnose connection issues since ant uses jdbc and the same URL
      that Scarab does while running.
      
NOTE: We realize that the Win32 script is not as good as the Unix
      script. Contributions to improve the script are appreciated.

-------------------------------------------------------------------------
| R U N N I N G  T H E  S A N D B O X                                   |
-------------------------------------------------------------------------

To run Tomcat from within the target directory that was created by 
following the steps above under building the sandbox, all you need to do 
is:

cd target
./scarab.sh     <-- Unix
bin\startup.bat <-- Win32

Then, in your web browser, go to:

    <http://my.server.com:8080/issues>

The following URL's also work the same by default:

    <http://my.server.com:8080/s>
    <http://my.server.com:8080/issue>
    <http://my.server.com:8080/scarab/servlet/scarab>

NOTE: Make sure that your TOMCAT_HOME is defined correctly. If you are 
      using the Tomcat that comes with Scarab, you can safely undefine
      this environment variable.

NOTE: Substitute 'my.server.com' for the DNS name that the server is
      running on.

NOTE: You can define your own URL by editing src/conf/web.xml and defining
      a different servlet mapping and then rebuilding.

-------------------------------------------------------------------------
| Q U E S T I O N S  /  P R O B L E M S                                 |
-------------------------------------------------------------------------

If you have problems or questions, please join the Scarab developer mailing 
list and post a detailed message describing your issues. :-)

<http://scarab.tigris.org/>
