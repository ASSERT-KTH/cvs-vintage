/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.logging;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import javax.management.*;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class Logger
   extends NotificationBroadcasterSupport
   implements LoggerMBean, MBeanRegistration, NotificationBroadcaster
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   long sequence = 0;
   Date now = new Date();
   
   ArrayList notificationListeners = new ArrayList();
   
   // Static --------------------------------------------------------
   static Logger logger;
   
   public static Logger getLogger() { return logger; }
   
   public static void log(String type, String message)
   {
      Log l = (Log)Log.getLog();
      l.log(type, message);
   }

   public static void log(String message)
   {
      Log l = (Log)Log.getLog();
      l.log(message);
   }

   public static void exception(Throwable exception)
   {
      Log l = (Log)Log.getLog();
      l.exception(exception);
   }

   public static void warning(String message)
   {
      Log l = (Log)Log.getLog();
      l.warning(message);
   }
	
   public static void debug(String message)
   {
      Log l = (Log)Log.getLog();
      l.debug(message);
   }

   public static void debug(Throwable exception)
   {
      Log l = (Log)Log.getLog();
      l.debug(exception.toString());
   }
   
   // Constructors --------------------------------------------------
   public Logger()
   {
      logger = this;
   }
   
   // Public --------------------------------------------------------
   public synchronized void fireNotification(String type, Object source, String message)
   {
      now.setTime(System.currentTimeMillis());
      Notification n = new Notification(type, this, sequence++, now, message);
      n.setUserData(source);
    
      sendNotification(n);
   }
   
   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      return new ObjectName("DefaultDomain:service=Log");
   }
   
   public void postRegister(java.lang.Boolean registrationDone) 
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception 
   {}
   
   public void postDeregister() {}

}

