$Id: README.txt,v 1.33 2002/03/11 21:43:12 jon Exp $

Welcome to Scarab!

The installation and configuration of Scarab is intended to be as simple as 
possible. This document outlines the directory structure of the distribution 
(currently it mirrors the CVS tree), the requirements for running Scarab,
the instructions for building and running an installation as well as 
requirements for setting up the database.

If you are doing development work with Scarab, please make sure to read 
the DEVELOPMENT.txt file for more hints on working with the CVS tree.

We appreciate your deciding to try Scarab out and appreciate constructive 
feedback on your success (or failure...we hope not!) in getting the 
system running.


-------------------------------------------------------------------------
| R E Q U I R E M E N T S                                               |
-------------------------------------------------------------------------

JDK 1.2 or higher        --> <http://java.sun.com/>
Ant 1.4.1 or higher      --> <http://jakarta.apache.org/ant/>
Tomcat 4.0.3 or higher   --> <http://jakarta.apache.org/tomcat/>
                             (Note: Tomcat is included with Scarab.)

MySQL 3.23 or higher     --> <http://www.mysql.org/>
                         or
Postgresql 7.2 or higher --> <http://www.postgresql.org/>


You must have the JAVA_HOME environment variable properly set to be the 
location of your JDK installation directory.

You must have Ant installed and ANT_HOME defined in your environment as
well as ANT_HOME/bin in your PATH.

The database is assumed to be installed and running with appropriately
configured access control setup (see below for more detail). You must
have the database binaries in your PATH (ie: $MYSQL_HOME/bin). 

With the Scarab communities help, we will be supporting a wide range of
databases in the released version of Scarab, however, the CollabNet 
developers are currently doing development primarily on MySQL and thus
do not guarantee that Scarab will work on other databases.

All of the necessary .jar files for building and running Scarab are
included in the /lib directory and the build system is setup to include
these into your classpath for you.

If you already have an existing webserver or service running on ports
8080 and 8005, and you are using Scarab's version of Tomcat, you will
need to change the port number to another unused port number by defining
the scarab.tomcat.http.port and scarab.tomcat.shutdown.port properties
in your build.properties (please see the "Settings" instructions below
for how to create a build.properties file). Once you have done this, you
will need to rebuild the sandbox.


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
    /i18n       <-- Location of internationalized files. Not much there yet.
    /images     <-- Copied to the webapp/images directory.
    /java       <-- The Java source code for Scarab.
    /resources  <-- Resources for the UI Tool. Not currently used.
    /sql        <-- SQL files for defining the database.
    /templates  <-- Velocity templates for the HTML and Email.
    /test       <-- Test suite code. Not much there yet.
    /tomcat-4.0 <-- A minimal copy of Tomcat 4.0 for use with the Scarab
                    sandbox.


-------------------------------------------------------------------------
| S E T T I N G S                                                       |
-------------------------------------------------------------------------

The Scarab build process depends on having a few properties which are
defined in the build/default.properties. The settings in the
build/default.properties are fairly well documented. These properties
should be set accordingly *before* you build Scarab.

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


-------------------------------------------------------------------------
| I N S T A L L I N G  T H E  D A T A B A S E                           |
-------------------------------------------------------------------------

The process of building Scarab creates the .sql file that is used to
describe the Scarab database schema. To install the database schema's,
you will need to install the database on your system and put the path to
the database binaries into your PATH environment variable.

Simply put, the idea is that the database creation scripts and the Java
database driver (JDBC) need the ability to connect to the database. In
order to do this, the code needs to be told a host machine, database
name, username and password.

By default, the scripts assume a database called 'scarab' and no
username/password to connect to the database on localhost. For example,
if you have MySQL up and running with no username/password for localhost
access, you can simply execute the following:

    cd src/sql
    ./create-db.sh mysql         <-- Unix
    create-mysql-database.bat    <-- Win32

If you need to specify a host/username/password/databasename, you will
need to specify command line arguments to the create-db.sh script (Unix)
or edit the create-mysql-database.bat script (Win32) in order to specify
these settings to the MySQL client. For example:

    cd src/sql
    ./create-db.sh -u jon --password -p 3306 -h mysql.server.com mysql

Also make sure to define your own scarab.database.* properties in your
local build.properties (see above for the explanation about how to use
build.properties) based on what is in the
scarab/build/default.properties. Once you have done this, you will need
to build the sandbox again in order to generate the right configuration
files based on this information.

NOTE: More detailed instructions for setting up the database on
      different database vendors is available on our website.
      
      <http://scarab.tigris.org/project_docs.html>
      
NOTE: The create scripts will attempt to first drop a database called
      "scarab" and then re-create it. If you execute this create-*
      script, all of your previous data in that specific database will
      be lost without warning!

NOTE: If you get a 'Server configuration denies access to data source'
      or 'access denied' or 'Invalid authorization' error from MySQL in
      the log files, please read the MySQL documentation on how to fix
      this error. We will not provide support for this since it is
      really a MySQL configuration issue. Here is a couple links to help
      you solve your problem:
      
      <http://www.mysql.com/documentation/mysql/bychapter/
       manual_MySQL_Database_Administration.html#Access_denied>

      <http://sourceforge.net/docman/display_doc.php?docid=8968&group_id=15923>

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

    <http://localhost:8080/scarab/servlet/scarab>

NOTE: Make sure that your TOMCAT_HOME is defined correctly. If you are 
      using the Tomcat that comes with Scarab, you can safely undefine
      this environment variable.

NOTE: Substitute 'localhost' for the DNS name that the server is running
      on.


-------------------------------------------------------------------------
| Q U E S T I O N S  /  P R O B L E M S                                 |
-------------------------------------------------------------------------

If you have problems or questions, please join the Scarab developer mailing 
list and post a detailed message describing your issues. :-)

<http://scarab.tigris.org/>
