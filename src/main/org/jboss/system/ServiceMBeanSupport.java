/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.util.Date;

import javax.management.NotificationBroadcasterSupport;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.jboss.logging.Logger;
import org.apache.log4j.NDC;

/**
 * An abstract base class JBoss services can subclass to implement a
 * service that conforms to the ServiceMBean interface. Subclasses must
 * override {@link #getName} method and should override 
 * {@link #startService}, and {@link #stopService} as approriate.
 *
 * @see ServiceMBean
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.7 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20010619 scott.stark:</b>
 * <ul>
 * <li> use the full service class name as the log4j log name
 * </ul>
 * <p><b>20011202 Andreas Schaefer:</b>
 * <ul>
 * <li> Add the own MBean Service Name to be remembered in an attribute
 * </ul>
 */
public abstract class ServiceMBeanSupport
   extends NotificationBroadcasterSupport
   implements ServiceMBean, MBeanRegistration
{
   // Attributes ----------------------------------------------------

   private int state;
   private MBeanServer server;
   /** Own Object Name this MBean is registered with, see {@link #preRegister preRegister()}. **/
   private ObjectName mServiceName;
   private int id = 0;

   protected Logger log;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public ServiceMBeanSupport()
   {
      log = Logger.getLogger(getClass());
   }

   // Public --------------------------------------------------------

   public abstract String getName();

	public ObjectName getServiceName() {
      return mServiceName;
   }
   
   public MBeanServer getServer()
   {
      return server;
   }

   public int getState()
   {
      return state;
   }
   
   public String getStateString()
   {
      return states[state];
   }
 
   public Logger getLog()
   {
      return log;
   }

   /*   public void init()
      throws Exception
   {
      NDC.push(getName());
      log.info("Initializing");
      try
      {
         initService();
      }
      catch (Exception e)
      {
         log.error("Initialization failed", e);
         throw e;
      }
      finally
      {
         NDC.pop();
      }
      log.info("Initialized");
   }
   */
   public void start()
      throws Exception
   {
      if (getState() != STOPPED)
         return;
			
      state = STARTING;
      //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" starting", "State", "java.lang.Integer", new Integer(STOPPED), new Integer(STARTING)));
      log.info("Starting");
      NDC.push(getName());
      try
      {
         startService();
      }
      catch (Exception e)
      {
         state = STOPPED;
         //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
         sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" stopped", "State", "java.lang.Integer", new Integer(STARTING), new Integer(STOPPED)));
         log.error("Stopped", e);
         throw e;
      }
      finally
      {
         NDC.pop();
      }
      state = STARTED;
      //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" started", "State", "java.lang.Integer", new Integer(STARTING), new Integer(STARTED)));
      log.info("Started");
   }
   
   public void stop()
   {
      if (getState() != STARTED)
         return;
	
      state = STOPPING;
      //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" stopping", "State", "java.lang.Integer", new Integer(STARTED), new Integer(STOPPING)));
      log.info("Stopping");
      NDC.push(getName());
      
      try
      {
         stopService();
      }
      catch (Throwable e)
      {
         log.error(e);
      }
      
      state = STOPPED;
      //AS It seems that the first attribute is not needed anymore and use a long instead of a Date
      sendNotification(new AttributeChangeNotification(this, id++, new Date().getTime(), getName()+" stopped", "State", "java.lang.Integer", new Integer(STOPPING), new Integer(STOPPED)));
      log.info("Stopped");
      NDC.pop();
   }

   public void init() throws Exception
   {
      throw new Exception("Don't call init");
   }

   public void destroy() {};
   /*
   public void destroy()
   {
      if (getState() != STOPPED)
         stop();
	
      log.info("Destroying");
      NDC.push(getName());
      try
      {
         destroyService();
      } catch (Exception e)
      {
         log.error(e);
      }
   	
      log.info("Destroyed");
      NDC.pop();
   }
   */
   
   /**
   * Callback method of {@link javax.management.MBeanRegistration MBeanRegistration}
   * before the MBean is registered at the JMX Agent.
   * <br>
   * <b>Attention</b>: Always call this method when you overwrite it in a subclass
   *                   because it saves the Object Name of the MBean.
   *
   * @param server Reference to the JMX Agent this MBean is registered on
   * @param name Name specified by the creator of the MBean. Note that you can
   *             overwrite it when the given ObjectName is null otherwise the
   *             change is discarded (maybe a bug in JMX-RI).
   **/
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      ObjectName lName = getObjectName(server, name);
      if( name == null ) {
         mServiceName = lName;
      } else {
         mServiceName = name;
      }
      this.server = server;
      return lName;
   }

   public void postRegister(Boolean registrationDone)
   {
      if (!registrationDone.booleanValue())
      {
         log.info( "Registration is not done -> destroy" );
         stop();
      }
   }
   
   public void preDeregister()
      throws Exception
   {
   }
   
   public void postDeregister()
   {
      stop();//this is presumably redundant.... ServiceController should have called
      //stop already.
   }
   
   // Protected -----------------------------------------------------
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name;
   }
   
   protected void startService()
      throws Exception
   {
   }
   
   protected void stopService()
   {
   }
	
}
