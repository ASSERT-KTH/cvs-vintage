--------------------------------------------------------------------------
$Id: README.txt,v 1.1 2000/12/18 05:03:27 jon Exp $
--------------------------------------------------------------------------

The files in this directory are here for your convenience in building
and using Scarab.

--------------------------------------------------------------------------

CORE LIBRARIES
==============
These libraries are critical to the basic Scarab functionality and are 
required no matter what optional services you use.

* activation-*.jar

  JavaBeans Activation Framework. Required by JavaMail.  Part of Java
  2 Enterprise Edition.

  http://java.sun.com/products/javabeans/glasgow/jaf.html

* ecs-*.jar

  Element Construction Set, used to generate markup (HTML, XML) from
  Java code without using print statements.

  http://java.apache.org/ecs/

* servlet-*.jar

  This is the Servlet API 2.0 or greater. We include version 2.2 of the
  Servlet API with Turbine for building purposes. It is however recommended
  that you use the Servlet API that came with your Servlet Engine for deployment
  though.

  http://jakarta.apache.org/

* mail-*.jar

  Java Mail.

  http://java.sun.com/products/javamail/index.html

* village-*.jar

  A Java interface to databases via JDBC drivers.

  http://www.working-dogs.com/village/

* velocity-*.jar

  A templating engine that will soon replace WebMacro.

  http://jakarta.apache.org/velocity/


BUILD TOOLS
===========
These libraries are used when building Turbine and its documentation. These are
not necessary for the operation of Turbine itself.

* ant-*.jar (in ../build)

  Java build tool.

  http://jakarta.apache.org/ant/

* mysql-*.jar

  MM MySQL JDBC Driver
  
  http://www.worldserver.com/mm.mysql/

* xmlrpc.jar

  XML Remote Procedure Calls: handles remote procedure calls
  implemented through the passing of XML messages.

  http://www.xmlrpc.org/
