/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @version $Revision: 1.4 $
 */
public class SystemProperties
   implements SystemPropertiesMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=SystemProperties";
    
   // Attributes ----------------------------------------------------
   
   Log log = Log.createLog("System properties");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public SystemProperties(String resource)
   {
      // Load system properties from resource
      try
      {
         System.getProperties().load(getClass().getClassLoader().getResourceAsStream(resource));
         log.log("System properties loaded from:"+resource);
      } catch (Exception e)
      {
         log.error("Could not load system properties from "+resource);
         log.exception(e);
      }
   }
   
   // Public --------------------------------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      return new ObjectName(OBJECT_NAME);
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
   }
   
   public void postDeregister()
   {
   }
   // Protected -----------------------------------------------------
}

