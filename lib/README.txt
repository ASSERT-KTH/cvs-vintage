--------------------------------------------------------------------------
$Id: README.txt,v 1.10 2004/10/28 20:16:33 dabbous Exp $
--------------------------------------------------------------------------

Starting from Scarab-1.0-b20 the distribution does not ship any other 
files within this directory except this README.txt.

All library dependencies for Scarab are now fully resolved and maintained
by Maven. But in order to keep support of ant builds and local maven 
builds in parallel, we generate and ship an up to date maven-compatible 
repository which contains all necessary jar libraries for Scarab. The
repository can be found in

    www/repository/...  (relative to the distribution root)

The ant build system has been updated to use the necessary
resources from that repository. 


If you use Maven as your primary build system, you don't need to,
but you can add www/repository to your Maven runtime resources.
Maven can resolve the necessary dependencies by itself.

   [FIXME: how to tell maven about the repo]

If you use ant as your primary build system, the files in 
www/repository are here for your convenience in building
and using Scarab. The versions included with Scarab are known to be
working with Scarab. If you decide to use another version of these
libraries, unless otherwise noted (like with servlet.jar), no support
will be provided. 

If you need additional libraries, which are not distributed by Scarab
but needed for your Scarab customization, please place these libraries
into this directory (/lib). The build process will pick them up and
copy them to their final destination.


The remainder of this file describes the libraries, which are used
by Scarab.

--------------------------------------------------------------------------
[FIXME]: The description is partially outdated)

CORE LIBRARIES
==============
These libraries are critical to the basic Scarab functionality and are
required no matter what optional services you use. Many of the JAR files
are included as a result of one JAR depending on another JAR for
functionality. In nearly every case, we attempt to focus our development
to only rely on Share.whichever.com, Sun and Jakarta Apache JAR files
because these projects are well known to the open source community and
considered 'standard' and well supported in our community circles.

* activation-*.jar

  JavaBeans Activation Framework. Required by JavaMail.  Part of Java
  2 Enterprise Edition.

  http://java.sun.com/products/javabeans/glasgow/jaf.html

* commons-beanutils.jar

  Dynamic access to Java object properties.

  http://jakarta.apache.org/commons/beanutils.html 

* commons-betwixt.jar

  Utility that makes Digester useful

  http://jakarta.apache.org/commons/betwixt/ 

* commons-collections.jar

  There are certain holes left unfilled by Sun's implementations of
  Collections, and the Jakarta-Commons Collections Component strives to
  fulfill them.

  http://jakarta.apache.org/commons/collections.html

* commons-digester*.jar

  The Digester package lets you configure an XML -> Java object mapping
  module, which triggers certain actions called rules whenever a
  particular pattern of nested XML elements is recognized.

  http://jakarta.apache.org/commons/digester.html  

* commons-email*.jar

  A thin wrapper around JavaMail to make it easier to use.

  http://cvs.apache.org/viewcvs.cgi/jakarta-commons-sandbox/email/

* commons-util*.jar

  Re-usable utility code.

  http://cvs.apache.org/viewcvs.cgi/jakarta-commons-sandbox/util/

* commons-http*.jar

  Re-usable http utility code. (Used in turbine.)

  http://cvs.apache.org/viewcvs.cgi/jakarta-commons-sandbox/http/

* commons-io*.jar

  Re-usable io utility code. (Used in stratum.)

  http://cvs.apache.org/viewcvs.cgi/jakarta-commons-sandbox/io/

* commons-codec*.jar

  Re-usable codec utility code. (Used in fulcrum.)

  http://cvs.apache.org/viewcvs.cgi/jakarta-commons-sandbox/util/

* dom4j*.jar

  dom4j is an easy to use, open source library for working with XML,
  XPath and XSLT on the Java platform using the Java Collections
  Framework and with full support for DOM, SAX and JAXP.

  http://www.dom4j.org/

* dnsjava.jar

  This is a great DNS package implemented in Java. We optionally
  use it in the registration area to check to see if an email address
  has a valid A record for the domain.
  
  http://www.xbill.org/dnsjava/

* flux*.jar

  We are phasing this one out. It is a webapp built for Turbine
  that allows one to manage users/roles/permissions.
  
  http://jakarta.apache.org/turbine/

* fulcrum*.jar

  Singleton Services framework. Part of the Jakarta Turbine Project.
  
  http://jakarta.apache.org/turbine/

* jakarta-oro*.jar

  heavyweight regex package
  
  http://jakarta.apache.org/oro/

* jakarta-regexp*.jar

  lightweight regex package
  
  http://jakarta.apache.org/regexp/

* JavaGroups-2.0.jar

  group communication toolkit based on IP multicast, used by JCS (cache)

  http://javagroups.sourceforge.net/

* jdbc2_0-stdext.jar

  JDBC 2.0 Extension API.

  http://java.sun.com/products/jdbc/

* junit*.jar

  Java Unit Testing. Used for our testing framework.

  http://www.junit.org/

* log4j-1.1.jar

  Log4J. Most Excellent Logging package.

  http://jakarta.apache.org/log4j/

* lucene*.jar

  Java Search Engine
  
  http://jakarta.apache.org/lucene/
  
* mail-*.jar

  JavaMail. Used for sending email from Java.

  http://java.sun.com/products/javamail/index.html

* servlet-*.jar

  This is the Servlet API 2.2 or greater. We include version 2.2 of the
  Servlet API with Turbine for building purposes. It is however
  recommended that you use the Servlet API that came with your Servlet
  Engine for deployment though.

  http://jakarta.apache.org/

* stratum*.jar

  Turbine dependency
  
  http://jakarta.apache.org/turbine/

* torque*.zip

  Torque is our object relational tool for talking to the database. This
  .zip file includes the entire Torque distribution, including a
  torque.jar. This project is part of the Turbine project.

  http://jakarta.apache.org/turbine/

* turbine*.jar

  Turbine 3 is our webapp framework.

  http://jakarta.apache.org/turbine/
  
* velocity-*.jar

  A templating engine that will soon replace WebMacro.

  http://jakarta.apache.org/velocity/

* village-*.jar

  A Java interface to databases via JDBC drivers.

  http://share.whichever.com/village/

* xalan*.jar

  XSLT stylesheet transformation
  
  http://xml.apache.org/xalan-j/

* xerces*.jar

  XML parser
  
  http://xml.apache.org/xerces-j/
  

DATABASE JDBC DRIVERS
=====================

* mm.mysql-*.jar

  MM MySQL JDBC Driver
  
  http://mmmysql.sourceforge.net/

* postgresql-*.jar

  Postgresql JDBC Driver

  http://jdbc.postgresql.org/download.html


BUILD TOOLS
===========
These libraries are used when building Scarab. These are not necessary
for the operation of Scarab itself.

* ant-*.jar (in ../build)

  Java build tool.

  http://jakarta.apache.org/ant/

