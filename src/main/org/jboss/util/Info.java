/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author Scott_Stark@displayscape.com
 *   @version $Revision: 1.5 $
 */
public class Info
   implements InfoMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Info";
    
   // Attributes ----------------------------------------------------
   
   Log log = Log.createLog("Info");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      // Dump out basic info as INFO priority msgs
      log.log("Java version: "+System.getProperty("java.version")+","+System.getProperty("java.vendor"));
      log.log("Java VM: "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version")+","+System.getProperty("java.vm.vendor"));
      log.log("System: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+","+System.getProperty("os.arch"));
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


