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

import org.jboss.logging.Log;
import org.jboss.logging.DefaultLog;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @version $Revision: 1.5 $
 */
public class Executor
   implements ExecutorMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Executor";
    
   // Attributes ----------------------------------------------------
   String exec;
   
    Log log = Log.createLog("Executor");
   
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
      log.log("Execute:"+exec);
      Process p = Runtime.getRuntime().exec(exec);
      
      p.getErrorStream().close();
      DataInputStream in = new DataInputStream(p.getInputStream());
      String line;
      while ((line = in.readLine()) != null)
         log.log(line);
      
      p.waitFor();
      log.log("Done");
         
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


