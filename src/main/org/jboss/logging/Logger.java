/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
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
 * @deprecated, As of JBoss 2.3, replaced by the org.apache.log4j framework
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.11 $
 */
public class Logger
   extends NotificationBroadcasterSupport
   implements LoggerMBean, MBeanRegistration, NotificationBroadcaster, Runnable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   long sequence = 0;
   Date now = new Date();
	
	boolean running = true;
   
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
      l.debug(exception);
   }
   
   public static void error(String message) 
   {
	  Log l = (Log) Log.getLog();
	  l.error(message);
  }
  
  
   public static void error(Throwable exception) 
   {
	  Log l = (Log) Log.getLog();
	  l.error(exception.toString());
  }
   
   // Constructors --------------------------------------------------
   public Logger()
   {
      logger = this;
		
		Thread runner = new Thread(this, "Log time updater");
		runner.setDaemon(true);
		runner.start();
   }
   
   // Public --------------------------------------------------------
   public synchronized void fireNotification(String type, Object source, String message)
   {
       //AS FIXME Just a hack (now.getTime())
	   Notification n = new Notification(type, this, sequence++, now.getTime(), message);
      n.setUserData(source);
    
      sendNotification(n);
   }
   
   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      return name == null ? new ObjectName("JBOSS-SYSTEM:spine=Log") : name;
   }
   
   public void postRegister(java.lang.Boolean registrationDone) 
   {
   }
   
   public void preDeregister()
      throws java.lang.Exception 
   {}
   
   public void postDeregister() 
	{
		running = false;
	}

   // Runnable implementation ---------------------------------------
	public void run()
	{
		while (running)
		{
			now.setTime(System.currentTimeMillis());
		
			try
			{
				Thread.sleep(5*1000);
			} catch (InterruptedException e)
			{
				// Ignore
			}
		}
	}
}

