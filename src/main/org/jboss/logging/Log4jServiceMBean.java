/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import javax.management.ObjectName;

import org.jboss.util.ObjectNameFactory;

/**
 * Management interface for log4j
 *
 * @author <a href="mailto:phox@galactica.it">Fulco Muriglio</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.4 $
 */
public interface Log4jServiceMBean
{
   /** The default JMX object name for this MBean. */
   ObjectName OBJECT_NAME =
      ObjectNameFactory.create("jboss.system:service=Logging,type=Log4J");

   /**
    * Get the log4j.properties format config file path
    */
   String getConfigurationPath();

   /**
    * Set the log4j.properties format config file path
    */
   void setConfigurationPath(String path);

   /**
    * Get the refresh flag. This determines if the log4j.properties file
    * is reloaded every refreshPeriod seconds or not.
    */  
   boolean getRefreshFlag();

   /**
    * Set the refresh flag. This determines if the log4j.properties file
    * is reloaded every refreshPeriod seconds or not.
    */
   void setRefreshFlag(boolean flag);

   /**
    * Configures the log4j framework using the current service properties
    * and sets the service category to the log4j root Category.
    */
   void start() throws Exception;
   
   /**
    * Stops the log4j framework by calling the Category.shutdown() method.
    * 
    * @see org.apache.log4j.Category#shutdown
    */
   void stop();
}
