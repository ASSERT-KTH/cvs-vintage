/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class Info
   implements InfoMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Info";
    
   // Attributes ----------------------------------------------------
   
   Log log = new Log("Info");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      log.log("Java version: "+System.getProperty("java.version")+","+System.getProperty("java.vendor"));
      log.log("Java VM: "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version")+","+System.getProperty("java.vm.vendor"));
      log.log("System: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+","+System.getProperty("os.arch"));
         
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

