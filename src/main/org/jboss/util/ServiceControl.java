/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.management.*;

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class ServiceControl
   implements ServiceControlMBean, MBeanRegistration, NotificationListener
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=ServiceControl";
    
   // Attributes ----------------------------------------------------
   Log log = new Log("Service Control");
   
   List mbeans = new ArrayList();
   MBeanServer server;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Service implementation ----------------------------------------
   public void init()
      throws Exception
   {
      log.log("Initializing "+mbeans.size()+" MBeans");
      
      List mbeansCopy = new ArrayList(mbeans);
      Iterator enum = mbeansCopy.iterator();
      int serviceCounter = 0;
      while (enum.hasNext())
      {
         ObjectName name = (ObjectName)enum.next();
         try
         {
            server.invoke(name, "init", new Object[0], new String[0]);
            serviceCounter++;
            
            // Register start/stop listener
            server.addNotificationListener(name,
                                          this,
                                          null,
                                          name);
         } catch (ReflectionException e)
         {
           // Not a service - ok 
         } catch (Exception e)
         {
            log.error("Could not initialize "+name);
            log.exception(e);
         }
      }
      log.log("Initialized "+mbeansCopy.size()+" services");
   }
   
   public void start()
      throws Exception
   {
      log.log("Starting "+mbeans.size()+" MBeans");
      
      List mbeansCopy = new ArrayList(mbeans);
      Iterator enum = mbeansCopy.iterator();
      int serviceCounter = 0;
      while (enum.hasNext())
      {
         ObjectName name = (ObjectName)enum.next();
         
         try
         {
            server.invoke(name, "start", new Object[0], new String[0]);
            serviceCounter++;
         } catch (ReflectionException e)
         {
           // Not a service - ok 
         } catch (Throwable e)
         {
            log.error("Could not start "+name);
            
            if (e instanceof RuntimeErrorException)
            {
               e = ((RuntimeErrorException)e).getTargetError();
            }
            
            log.exception(e);
         }
      }
      log.log("Started "+mbeansCopy.size()+" services");
   }
   
   public void stop()
   {
      
   }
   
   public void destroy()
   {
      
   }
   
   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      this.server = server;
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }
   
   public void postRegister(java.lang.Boolean registrationDone)
   {
      try
      {
         server.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"),
                                       this,
                                       null,
                                       null);
         log.log("Registered with server");
      } catch (Exception e)
      {
         log.error("Could not register with server");
         log.exception(e);
      }
   }
   
   public void preDeregister()
      throws java.lang.Exception
   {
      server.removeNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), this);
      log.log("Deregistered from server");
   }
   
   public void postDeregister()
   {
   }
   
   // NotificationListener implementation ---------------------------
   public void handleNotification(Notification notification,
                               java.lang.Object handback)
   {
      if (notification instanceof AttributeChangeNotification)
      {
         AttributeChangeNotification attrChg = (AttributeChangeNotification)notification;
//         log.log(handback+":"+attrChg.getAttributeName()+":"+attrChg.getNewValue());
      } else
      {
         MBeanServerNotification reg = (MBeanServerNotification)notification;
      
         if (reg.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
         {
            mbeans.add(reg.getMBeanName());
         } else if (reg.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
         {
            mbeans.remove(reg.getMBeanName());
         }
      }
   }
}

