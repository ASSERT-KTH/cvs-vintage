$Id: README.txt,v 1.24 2002/01/17 02:49:11 dlr Exp $

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

JDK 1.2 or higher       --> <http://java.sun.com/>
Ant 1.4.1 or higher     --> <http://jakarta.apache.org/ant/>
MySQL 3.22 or higher    --> <http://www.mysql.org/>
Tomcat 4.0.1 or higher  --> <http://jakarta.apache.org/tomcat/>
                            (Note: Tomcat is included with Scarab.)

You must have the JAVA_HOME environment variable properly set to be the 
location of your JDK installation directory.

You must have Ant installed and ANT_HOME defined in your environment as
well as ANT_HOME/bin in your PATH.

MySQL is assumed to be installed and running with appropriately
configured access control setup (see below for more detail). You must
have the MySQL binaries in your PATH. We will be supporting a wide range
of databases in the released version of Scarab, however, we are
currently doing development primarily on MySQL.

All of the necessary .jar files for building and running Scarab are included 
in the /lib directory and the build system is setup to include these into 
your classpath for you.

If you already have an existing webserver running on port 8080, and you
are using Scarab's version of Tomcat, you will need to change the port
number to another unused port number by editing the
src/tomcat-4.0/conf/server.xml and changing the 8080 to something else.
Once you have done this, you will need to rebuild the sandbox.


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
    /dtd        <-- Intake and Torque DTD's.
    /html       <-- Files which show up within the webapp directory.
    /images     <-- Copied to the webapp/images directory.
    /java       <-- The Java source code for Scarab.
    /resources  <-- Resources for the UI Tool. Not currently used.
    /sql        <-- SQL files for defining the database.
    /templates  <-- Velocity templates for the HTML and Email.
    /tomcat-4.0 <-- A minimal copy of Tomcat 4.0 for use with the Scarab
                    sandbox.
    /usecases   <-- Old usecase documentation. Currently out of date.


-------------------------------------------------------------------------
| S E T T I N G  T H E  M A I L S E R V E R                             |
-------------------------------------------------------------------------

In order to test/use the functionality of the Login and Registration
process, you need to first set the outgoing mail server so that email
can be sent. By default, it is defined as "localhost". That means that
you need to have an email server running on the same box as Scarab. It
is possible to modify this value by changing the "mail.server" property
in the src/conf/TurbineResources.properties file to name of a mail
server that allows you to relay email through it.

NOTE: If you modify this value after you have build the sandbox
      (instructions below), then you will need to re-run the ant
      script and restart the servlet engine in order for the changes to
      take effect.


-------------------------------------------------------------------------
| B U I L D I N G  T H E  S A N D B O X                                 |
-------------------------------------------------------------------------

To build the sandbox on your machine, you simply need to do the following:

cd build
ant

This will create a directory in the scarab directory called "target".
Within there will be pretty much everything that you need to get started
with running Scarab. You can safely remove this directory at any point
as the source files for creating this directory are in the /lib and /src
directory. If you edit any of the files in the /lib or /src directory,
then you should simply re-run the ant script and it will deal with
copying and compiling the changed files.

NOTE: Make sure that your TOMCAT_HOME is defined correctly. If you are
      using the Tomcat that comes with Scarab, you can safely undefine
      it and follow the directions below for running the sandbox.

NOTE: If you already have an existing Tomcat installation and prefer
      to run Scarab from there, first build Scarab and then copy the 
      target/webapps/scarab directory into your own Tomcat installation.

NOTE: If you already have an existing webserver running on port 8080,
      and you are using Scarab's version of Tomcat, you will need to
      change the port number to another unused port number by editing
      the src/tomcat-4.0/conf/server.xml and changing the 8080 to
      something else. Once you have done this, you will need to rebuild
      the sandbox.
      
NOTE: There may be problems building and running Scarab with Tomcat 3.2.1. 
      We have not done this testing yet. If your current Tomcat
      installation is not 4.0, you can either compile to the default
      target/ directory or wait until 4.0 is formally released.


-------------------------------------------------------------------------
| I N S T A L L I N G  T H E  D A T A B A S E                           |
-------------------------------------------------------------------------

The process of building Scarab creates the .sql file that is used to
describe the Scarab database schema. To install the database schema's,
you will need to install MySQL and put the path to the mysqladmin and
mysql binaries into your PATH environment variable. Once you have done
that and you have MySQL up and running with no username/password for
localhost access, you can simply execute the following:

cd target/webapps/scarab/WEB-INF/sql
./create-mysql-database.sh   <-- Unix
create-mysql-database.bat    <-- Win32

NOTE: This will attempt to first drop a database called "scarab" and 
      then re-create it. If you execute this script, all of your previous data 
      will be lost without warning!

NOTE: If you need to specify a host/username/password, you will need to 
      edit the create-mysql-database.sh/.bat script to specify these to
      the MySQL client. We have added a feature to the .sh script which
      prevents you from needing to edit it, simply pass the
      username/password in as arguments when you execute it.
      
      ./create-mysql-database.sh USERNAME PASSWORD DATABASE_NAME
      
      Also make sure to edit the src/conf/TurbineResources.properties
      file and modify the database.default.url,
      database.default.username and database.default.password
      properties. Once you have done this, you will need to build the
      sandbox again in order to copy the TR.props file to the right
      location.

NOTE: If you get an access denied error from MySQL, please read the MySQL
      documentation on how to fix this error. We will not provide
      support for this since it is really a MySQL configuration issue.
      Here is a link to help you solve your problem:
      
      <http://www.mysql.com/documentation/mysql/bychapter/
       manual_MySQL_Database_Administration.html#Access_denied>

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

NOTE: Make sure that your TOMCAT_HOME is defined correctly. If you are using 
      the Tomcat that comes with Scarab, you can safely undefine it.


-------------------------------------------------------------------------
| Q U E S T I O N S  /  P R O B L E M S                                 |
-------------------------------------------------------------------------

If you have problems or questions, please join the Scarab developer mailing 
list and post a detailed message describing your issues. :-)

<http://scarab.tigris.org/>
