$Id: README.txt,v 1.66 2004/02/11 12:06:10 dep4b Exp $

Welcome to Scarab!

The installation and configuration of Scarab is intended to be as simple as 
possible. This document outlines the directory structure of the distribution 
(currently it mirrors the CVS tree), the requirements for running Scarab,
the instructions for building and running an installation as well as 
requirements for setting up the database.

If you are doing development work with Scarab, please make sure to read 

        http://scarab-gen.tigris.org/nonav/HEAD/
        http://scarab-gen.tigris.org/nonav/HEAD/howto/build-howto.html

We appreciate your deciding to try Scarab out and appreciate constructive 
feedback on your success (or failure...we hope not!) in getting the 
system running.


,-----------------------------------------------------------------------.
| R E Q U I R E M E N T S                                               |
'-----------------------------------------------------------------------'

* SDK 1.3.1 or higher available from 

        http://java.sun.com/

        Make sure that you download the SDK and not the JRE!
        On some operating systems (OS X, for instance), 
        the SDK is included.

        When compiling Scarab with Jikes, version 1.18 or higher is
        required.  Please do not try with Jikes 1.17, as it is buggy.

* Ant 1.5 or higher available from 

        http://ant.apache.org/

        The version of torque used with scarab must be compiled 
        with Ant 1.5.x

* Tomcat 4.0.4 or higher available from 

        http://jakarta.apache.org/tomcat/

        Tomcat 4.1.x is included with Scarab.
        If you are using an existing Tomcat 4.1.x installation, 
        you MUST *move* the common/endorsed/xercesImpl.jar to 
        the server/lib directory. Please note that if you have 
        any existing web applications that depend on Xerces 2, 
        you will need to copy that .jar file into their WEB-INF/lib 
        in order to make things work properly after the move.


* An RDBMS preferably one out of the list below

  - MySQL 3.23.x/4.x  available from 

        http://www.mysql.org/

        If you want to use the faster/newer JDBC driver with MySQL, 
        you can download it from the MySQL website and use them 
        instead. We do not distribute it because it is GPL. 
        To use it, just copy the .jar file into your scarab/lib 
        directory and put this in your build.properties file: 

        scarab.jdbc.driver.jar=mysql-connector*.jar
      
        see: <http://www.mysql.com/downloads/api-jdbc-dev.html>

        Scarab will eventually require the transactional support 
        present only in MySQL version 4 and higher, and will drop 
        support for the transaction-less versions 3.23.x and lower.  
        If getting started using MySQL, it's recommended that you 
        install version 4 or higher.

  - Postgresql 7.3.x available from  

        http://www.postgresql.org/

  - hypersonic 1.7.1 available from 

        http://hsqldb.sourceforge.net


  NOTE: More detailed instructions for setting up the database on
        different database vendors are available on our website.

        !!! Please read the docs BEFORE building Scarab !!!

        http://scarab.tigris.org/project_docs.html


,-----------------------------------------------------------------------.
| E N V I R O N M E N T A L   S E T T I N G S                           |
'-----------------------------------------------------------------------'

JAVA_HOME

You must have the JAVA_HOME environment variable properly set to be the 
location of your SDK installation directory. On MacOSX, this path is:
/System/Library/Frameworks/JavaVM.framework/Home


ANT_HOME

You must have Ant installed and ANT_HOME defined in your environment as
well as ANT_HOME/bin in your PATH.


ANT_OPTS

You must enshure, that the ant process has enough free HEAP.
One convenient way of setting the appropriate JVM option is to
set the environment variable ANT_OPTS as follows:

        ANT_OPTS=-Xmx256m

This avoids build failures due to OutOfMemoryError Exceptions.


Database settings (for MYSQL)

The database is assumed to be installed and running with appropriately
configured access control setup (see below for more detail). You must
have the database binaries in your PATH (ie: $MYSQL_HOME/bin). 


Database settings for other RDBM's

With the Scarab communities help, we will be supporting a wide range of
databases in the released version of Scarab, however, the Scarab 
developers are currently doing development primarily on MySQL and thus
do not guarantee that Scarab will work on other databases.

example scripts

The following scripts can be used as templates for your own
environmental setup. The templates have been created for 
use wih MySQL. You must modify them according to your
environment:

    UNIX/LINUX sh/zsh/bash:
        export ANT_HOME=/path/to/ant-install
        export ANT_OPTS=-Xmx256m
        export MYSQL_HOME=/path/to/mysql-install
        export JAVA_HOME=/path/to/jdk-install
        export PATH=${PATH}:${ANT_HOME}/bin:${MYSQL_HOME}/bin:${JAVA_HOME}/bin

    	Put these settings into your ~/.bashrc or ~/.login file

    UNIX/LINUX csh/tcsh:
        setenv ANT_HOME /path/to/ant-install
        setenv ANT_OPTS -Xmx256m
        setenv MYSQL_HOME /path/to/mysql-install
        setenv JAVA_HOME /path/to/jdk-install
        setenv PATH ${PATH}:${ANT_HOME}/bin:${MYSQL_HOME}/bin:${JAVA_HOME}/bin
        
        Put these statements into your ~/.cshrc file
        
    WINDOWS:
        set ANT_HOME=\path\to\ant-install
        set ANT_OPTS=-Xmx256m
        set MYSQL_HOME=\path\to\mysql-install
        set JAVA_HOME=\path\to\jdk-install
        set PATH=%PATH%;%ANT_HOME%\bin;%MYSQL_HOME\bin;%JAVA_HOME%\bin

        Add these settings to your Environment ( start settings system )  


.jar files

All of the necessary .jar files for building and running Scarab are
included in the /lib and the /www/repository directories and the build 
system is setup to include these into your classpath for you. Please do 
not add any jar files to your CLASSPATH as it may cause compile errors.


port settings of the application server

If you already have an existing webserver or service running on ports
8080 and 8005, and you are using Scarab's version of Tomcat, you will
need to change the port number to another unused port number by editing
the /tomcat/conf/server.xml file.


file permission settings

By default, the web applications WEB-INF directory needs to have
permissions set so that the userid which the JVM is running under can
write into that directory.


XML-environment

NOTE: Make sure to use the copy of Xerces 1.x which is included 
with the Scarab distribution and make sure that no other copies 
of Xerces (especially Xerces 2.x) are in your JAVA_HOME, 
ANT_HOME or in your CLASSPATH. Otherwise, you will get build errors.


,-----------------------------------------------------------------------.
| D I R E C T O R Y  S T R U C T U R E                                  |
'-----------------------------------------------------------------------'

Here is a description of the Scarab directory tree:

scarab
  +- build      <-- This is where the Ant scripts are to build the sample webapp
  |                 and create the sql scripts.  Additionally Ant can load the sql scripts.
  +- extensions/usermods 
  |             <-- This where the builder can place modifications to the standard
  |                 distribution that will be incorporated into the build.
  |                 This simplifies the process of replacing *.vm files,
  |                 among other things.
  +- extensions/scripts
  |             <-- Helper shell scripts
  +- extensions/dtd
  |             <-- DTD's for validating XML files
  +- extensions/bugzilla
  |             <-- Scripts for converting from Bugzilla to Scarab
  +- extensions/migration
  |             <-- Ant task for migrating from b15 to b16
  +- lib
  +- www/repository
  |             <-- These are the directories storing JAR files used by Scarab.
  +- src        <-- This where the source files are stored.
  +- tomcat     <-- This is the included Tomcat configured to be ready to run with Scarab.
  +- www        <-- This is where the website homepage is stored
  +- xdocs      <-- xml versions of manuals (admin guide, user guide, howtos etc)

Within the /src directory you find following subdirectories...

src
  +- conf/conf  <-- Various configuration files for Scarab.
  |                 TurbineResources.properties and Scarab.properties
  |                 live here.  Copied to WEB-INF/conf
  +- conf/classes
  |             <-- Various configuration files copied directly into the classpath.
  +- java       <-- The Java source code for Scarab.
  +- sql        <-- SQL files for defining the database.
  +--webapp     <-- All the web resources required for Scarab like images, css files,
  |                 javascript files, html files, velocity templates.
  +--test       <-- Test suite code.


,-----------------------------------------------------------------------.
| B U I L D  P R O P E R T I E S                                        |
'-----------------------------------------------------------------------'

The Scarab build process depends on having a few properties which are
defined in the build/default.*.properties. These settings are fairly 
well documented within the files. These properties must be set 
accordingly *before* you build Scarab.

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

NOTE: The ~ character represents your user account home directory.

Chances are that you are going to have to define your own database and
mail server properties as well as a few other properties in the resulting
Scarab WAR file.  There is no need to edit the WEB-INF/conf/TurbineResources.properties
file in the WAR file.  Instead, put any properties you need to edit in 
WEB-INF/conf/CustomSettings.properties.  

Alternatively, you can define environment properties in your servlet's JNDI tree.  You 
can do this either via the Tomcat Admin application or by editing the appropriate files.
To add a property that won't be overriden by each new .WAR file you install, edit your
/tomcat/webapps/scarab.xml file and add entries to your <context/> entry for Scarab.  For 
example, to set the system.mail.host property you would add this line:

<Environment description="Mail Host to Use" name="system/mail/host" override="true" type="java.lang.String" value="127.0.0.1"/>

To set which database adapter to use you would add a line like this:

<Environment description="Database Adapter" name="torque/database/scarab/adapter" override="true" type="java.lang.String" value="oracle"/>



For more information about other sources of Configuration data, read about Commons-Configuration:

        http://jakarta.apache.org/commons/configuration/



,-----------------------------------------------------------------------.
| S E T T I N G  T H E  M A I L S E R V E R                             |
'-----------------------------------------------------------------------'

In order to use Scarab, you need to first set the relay outgoing mail
server so that email can be sent from Scarab. This is important for many
different aspects of Scarab, such as the confirmation email sent when a
user registers with the system. By default, the mail server is defined
as "localhost". That means that you need to have an SMTP server running
on the same box as Scarab. It is possible to modify this value by
changing the property system.mail.host in CustomSettings.properties and
uncommenting the property or using the JNDI tree as discussed above.

You will need to stop and start Scarab

,-----------------------------------------------------------------------.
| S E T T I N G  T H E  U P L O A D  D I R E C T O R Y                  |
'-----------------------------------------------------------------------'

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

It is also recommended to set the services.UploadService.repository property in
CustomSettings.properties as well. This property is used as a temporary location
for the uploaded data during the upload process. By default it is "WEB-INF".



,-----------------------------------------------------------------------.
| S E T T I N G  T H E  I N D E X E S  D I R E C T O R Y                |
'-----------------------------------------------------------------------'

Scarab uses Lucene to create searchable indexes for the issue data.
Lucene needs to be able to store its indexes somewhere on disk. If the
disk is low on space, it is recommended to put the indexes on another
disk. This can be done by setting the searchindex.path property in 
CustomSettings.properties.
This path defaults to "WEB-INF/index" and can be defined as either a
relative path or an absolute path.


,-----------------------------------------------------------------------.
| R U N N I N G   W I T H   S E R V L E T   2 . 2                        |
'-----------------------------------------------------------------------'

Non-current versions of some commonly used application servers may not
support the servlet 2.3 specification.  One such server is Websphere 4.0
which only supports the servlet 2.2 specification.

While the Scarab project does not intend to officially maintain
compatibility with the servlet 2.2 specification, there currently is only
a single problem area, involving the HttpRequest.setCharacterEncoding()
call in DetermineCharsetValve.  Removing this call weakens Scarab's
support for character set encoding, but it allows running Scarab with
Websphere 4.0 application server.

To implement this feature, add the following line to the CustomSettings.properties
file as documented above:

pipeline.default.descriptor=org/tigris/scarab/pipeline/scarab-pipeline22.xml


,-----------------------------------------------------------------------.
| B U I L D I N G  T H E  S A N D B O X                                 |
'-----------------------------------------------------------------------'

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


,-----------------------------------------------------------------------.
| I N S T A L L I N G  T H E  D A T A B A S E                           |
'-----------------------------------------------------------------------'

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

    cd build
    ant create-db

If you need to specify a host/username/password/databasename, you will
need to edit the build/build.properties.  See the Settings section
above.

Also make sure to define your own scarab.database.* properties in your
local build.properties (see above for the explanation about how to use
build.properties) based on what is in the
scarab/build/default.properties. You will also need to update the war file
by editing the CustomSettings.xml

NOTE: If you would like to only load the required database data and not
      the sample/default data, you can do so by passing skip.seed.data
      value or adding it to your build.properties:  
      ant create-db -Dskip.seed.data=true

NOTE: More detailed instructions for setting up the database on
      different database vendors is available on our website.

      <http://scarab.tigris.org/project_docs.html>

NOTE: The create scripts will attempt to first drop a database called
      "scarab" and then re-create it. If you execute ant create-db
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

      <http://sourceforge.net/docman/display_doc.php?docid=8968&group_id=15923>     .


,-----------------------------------------------------------------------.
| R U N N I N G  T H E  S A N D B O X                                   |
'-----------------------------------------------------------------------'

To run the preconfigured Tomcat that comes with Scarab, all you need to do 
is verify that the file /tomcat/webapps/scarab.xml has a <Context> docBase 
pointing to where you built Scarab (typically the value should be ../../target/scarab).
There are two different <Context> entries depending on how you are using Scarab for inplace
development or not.

cd tomcat/bin
scarab.sh     <-- Unix
startup.bat <-- Win32

Then, in your web browser, go to:

    <http://localhost:8080/scarab>


NOTE: Make sure that your TOMCAT_HOME is defined correctly. If you are 
      using the Tomcat that comes with Scarab, you can safely undefine
      this environment variable.

NOTE: Substitute 'localhost' for the DNS name that the server is
      running on for remote access.

NOTE: You can define your own URL by editing the WEB-INF/web.xml 
	  and defining a different servlet mapping.

,-----------------------------------------------------------------------.
| C U S T O M I Z I N G   S C A R A B                                   |
'-----------------------------------------------------------------------'
At times it may be useful to make minor modifications to Scarab without
applying those changes directly to the Scarab sources (since this
will cause CVS to report those differences, and even worse, the custom
modifications might get unintentionally checked into the Scarab CVS
repository by a Scarab developer).

The extensions/usermod directory structure allows this to be done fairly simply.

It contains three directories, lib, conf, and templates.  Any files in
the lib directory will be copied into the appropriate target
directory _after_ any such files in the standard distribution,
and will replace the standard distribution files.  Any files in the
template directory will be copied to the target template directory
_ONLY_IF_ the property "scarab.copy.templates" is set, again after
the files from the standard distribution. 
NOTE: It is the responsibility of the builder to ensure that any
      subsequent changes to modified Velocity macros are transferred
      to these replacements.


,-----------------------------------------------------------------------.
| D A T A  M I G R A T I O N                                            |
'-----------------------------------------------------------------------'

See the MIGRATION-README.txt file.


,-----------------------------------------------------------------------.
| Q U E S T I O N S  /  P R O B L E M S                                 |
'-----------------------------------------------------------------------'

If you have problems or questions, please join the Scarab developer mailing 
list and post a detailed message describing your issues. :-)

<http://scarab.tigris.org/>

