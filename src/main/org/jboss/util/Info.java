/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.util.Enumeration;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Category;

/** A simple mbean that dumps out info like the system properties, etc.
 *      
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 *   @version $Revision: 1.7 $
 */
public class Info
   implements InfoMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Info";
    
   // Attributes ----------------------------------------------------
   Category log = Category.getInstance(Info.class);

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      // Dump out basic info as INFO priority msgs
      log.info("Java version: "+System.getProperty("java.version")+","+System.getProperty("java.vendor"));
      log.info("Java VM: "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version")+","+System.getProperty("java.vm.vendor"));
      log.info("System: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+","+System.getProperty("os.arch"));
      // Now dump out the entire System properties as DEBUG priority msgs
      log.debug("+++ Full System Properties Dump");
      Enumeration names = System.getProperties().propertyNames();
      while( names.hasMoreElements() )
      {
          String pname = (String) names.nextElement();
          log.debug(pname+": "+System.getProperty(pname));
      }
         
	  // MF TODO: say everything that needs to be said here: copyright, included libs and TM, contributor and (C) jboss org 2000
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


