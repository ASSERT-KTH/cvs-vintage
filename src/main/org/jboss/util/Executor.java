/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;

import javax.management.*;
import javax.management.loading.MLet;

import org.jboss.logging.Logger;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>.
 *   @version $Revision: 1.6 $
 */
public class Executor
   implements ExecutorMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Executor";
   private static Logger log = Logger.create("Executor");
    
   // Attributes ----------------------------------------------------
   String exec;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public Executor(String exec)
   {
      this.exec = exec;
   }
   
   // Public --------------------------------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      log.info("Execute:"+exec);
      Process p = Runtime.getRuntime().exec(exec);
      
      p.getErrorStream().close();
      DataInputStream in = new DataInputStream(p.getInputStream());
      String line;
      while ((line = in.readLine()) != null)
         log.info(line);
      
      p.waitFor();
      log.info("Done");
         
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


