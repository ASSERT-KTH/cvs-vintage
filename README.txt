$Id: README.txt,v 1.4 2000/12/21 22:16:28 jon Exp $

Welcome to Scarab!

If you are on a Windows machine, we require you to first have Cygwin installed 
and you must run the .sh shell scripts from within the Cygwin bash shell. You 
can very easily download and install Cygwin from here:

<http://sources.redhat.com/cygwin/>


-------------------------------------------------------------------------
| D I R E C T O R Y  S T R U C T U R E
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


-------------------------------------------------------------------------
| B U I L D I N G  T H E  S A N D B O X
-------------------------------------------------------------------------

Preparation:

You MUST have the JAXP 1.0.1 jar files in your classpath before you can execute 
the build scripts. This means that you need to have jaxp.jar and parser.jar in 
your classpath. You can download these files from:

<http://java.sun.com/xml/download.html>

You also MUST have the JAVA_HOME environment variable properly set to be the 
location of your JDK 1.2 or higher installation directory.

Building:

To build the sandbox on your machine, you simply need to do the following:

cd build
./build.sh compile

This will create a directory in the scarab directory called "target". Within 
there will be pretty much everything that you need to get started with running 
Scarab. You can safely remove this directory at any point as the source files 
for creating this directory are in the /lib and /src directory. If you edit any 
of the files in the /lib or /src directory, then you should simply re-run the 
build.sh compile script and it will deal with copying and compiling the changed 
files.

If you already have an existing Tomcat installation and prefer to run
scarab from there, create a file called .ant.properties in the build
directory. The contents of this file should be a single line equating
the variable build.dir to the path to tomcat. For example:

build.dir=/usr/local/jakarta-tomcat

NOTE: There are problems running scarab with Tomcat 3.2.1. If your
current Tomcat installation is not 4.0, you can either compile to the
default target/ directory or wait until 4.0 is formally released. 

-------------------------------------------------------------------------
| I N S T A L L I N G  T H E  D A T A B A S E
-------------------------------------------------------------------------

To install the database schema's, right now, you will need to install
MySQL and put the path to the mysqladmin and mysql binaries into 
your PATH environment variable. Once you have done that and you have
MySQL up and running with no username/password for localhost access, 
you can simply execute the following:

cd src/sql
./create-database.sh

Caution: This will attempt to first drop a database called "scarab" and 
then re-create it. If you execute this script, all of your previous
data will be lost without warning!


-------------------------------------------------------------------------
| R U N N I N G  T H E  S A N D B O X
-------------------------------------------------------------------------

To run Tomcat from within the target directory, all you need to do is:

cd target
./bin/catalina.sh run

Then, in your web browser, go to:

<http://localhost:8080/scarab/servlet/scarab>

Please note: There will be a very long (up to a minute) delay for the first 
request to return a response. This is due to the way that Tomcat 4.0 is 
generating the session id. The Random Number Generator (RNG) takes forever to 
seed itself. After the RNG has seeded itself, further requests should be quite 
fast. We are working with the Tomcat team on providing an alternative way to 
have a not so secure RNG for development purposes that loads very quickly.


-------------------------------------------------------------------------
| Q U E S T I O N S  /  P R O B L E M S
-------------------------------------------------------------------------

If you have problems or questions, please join the Scarab developer mailing list 
and post a detailed message describing your issues. :-)

<http://scarab.tigris.org/>
