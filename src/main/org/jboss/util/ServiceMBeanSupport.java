/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.util.Date;

import javax.management.NotificationBroadcasterSupport;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
   
import org.jboss.logging.Log;
/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.7 $
 */
public abstract class ServiceMBeanSupport
   extends NotificationBroadcasterSupport
   implements ServiceMBean, MBeanRegistration, Service
{
   // Attributes ----------------------------------------------------
   private int state;
   
   public static String[] states = {"Stopped","Stopping","Starting","Started"};
   public static int STOPPED  = 0;
   public static int STOPPING = 1;
   public static int STARTING = 2;
   public static int STARTED  = 3;
   
   private int id = 0;
   
   protected Log log;
    
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------
   public abstract String getName();

   public int getState()
   {
      return state;
   }
   
   public String getStateString()
   {
      return states[state];
   }
   
	public void init()
		throws Exception
	{
	   log = new Log(getName());
   
		log.log("Initializing");
		log.setLog(log);
		try
		{
		   initService();
		} catch (Exception e)
		{
		   log.error("Initialization failed");
		   log.exception(e);
		   throw e;
		} finally
		{
		   log.unsetLog();
		}
		log.log("Initialized");
	}
	
   public void start()
      throws Exception
   {
      if (getState() != STOPPED)
      	return;
			
      state = STARTING;
	  //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" starting", "State", "java.lang.Integer", new Integer(STOPPED), new Integer(STARTING)));
      log.log("Starting");
      log.setLog(log);
      try
      {
         startService();
      } catch (Exception e)
      {
         state = STOPPED;
	     //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
         sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" stopped", "State", "java.lang.Integer", new Integer(STARTING), new Integer(STOPPED)));
         log.error("Stopped");
         log.exception(e);
         throw e;
      } finally
      {
         log.unsetLog();
      }
      state = STARTED;
      //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" started", "State", "java.lang.Integer", new Integer(STARTING), new Integer(STARTED)));
      log.log("Started");
   }
   
   public void stop()
   {
		if (getState() != STARTED)
			return;
	
      state = STOPPING;
      //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" stopping", "State", "java.lang.Integer", new Integer(STARTED), new Integer(STOPPING)));
      log.log("Stopping");
      log.setLog(log);
      
      try
      {
         stopService();
      } catch (Throwable e)
      {
         log.exception(e);
      }
      
      state = STOPPED;
      //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" stopped", "State", "java.lang.Integer", new Integer(STOPPING), new Integer(STOPPED)));
      log.log("Stopped");
      log.unsetLog();
   }
   
   public void destroy()
   {
		if (getState() != STOPPED)
			stop();
	
   	log.log("Destroying");
   	log.setLog(log);
   	try
   	{
   	   destroyService();
   	} catch (Exception e)
   	{
   	   log.exception(e);
   	}
   	
   	log.unsetLog();
   	log.log("Destroyed");
   }
	
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
		name = getObjectName(server, name);

		init();
		
      return name;
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
      if (!registrationDone.booleanValue())
         destroy();
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
   }
   
   public void postDeregister()
   {
	   destroy();
   }
   
   // Protected -----------------------------------------------------
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return name;
   }
   
   protected void initService()
      throws Exception
   {
   }
	
   protected void startService()
      throws Exception
   {
   }
   
   protected void stopService()
   {
   }
	
   protected void destroyService()
   {
   }
}
