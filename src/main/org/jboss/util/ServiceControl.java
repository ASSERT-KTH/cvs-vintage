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

import org.apache.log4j.Category;

/**
 * ServiceControl manages the JBoss services lifecycle.
 * 
 * @see org.jboss.util.Service
 *  
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author <a href="mailto:hugo@hugopinto.com">Hugo Pinto</a>
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.11 $
 */
public class ServiceControl
   implements ServiceControlMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=ServiceControl";

    
   // Attributes ----------------------------------------------------

   /** Instance logger. */
   private final Category log = Category.getInstance(this.getClass());
   
   List mbeans = new ArrayList();
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Service implementation ----------------------------------------
   public void init()
      throws Exception
   {
      log.info("Initializing "+mbeans.size()+" MBeans");
      
      List mbeansCopy = new ArrayList(mbeans);
      Iterator enum = mbeansCopy.iterator();
      int serviceCounter = 0;
      while (enum.hasNext())
      {
         Service service = (Service)enum.next();
         try
         {
            service.init();
            serviceCounter++;
         }
         catch(Throwable e)
         {
            log.error("Could not initialize "+service, e);
         }
      }
      log.info("Initialized "+mbeansCopy.size()+" services");
   }
   
   public void start()
      throws Exception
   { 
      log.info("Starting "+mbeans.size()+" MBeans");
      
      List mbeansCopy = new ArrayList(mbeans);
      Iterator enum = mbeansCopy.iterator();
      int serviceCounter = 0;
      while (enum.hasNext())
      {
         Service service = (Service)enum.next();
         
         try
         {
            service.start();
            serviceCounter++;
         }
         catch(Throwable e)
         {
            log.error("Could not start "+service, e);
         }
      }
      log.info("Started "+mbeansCopy.size()+" services");
   }
   
   public void stop()
   {
      log.info("Stopping "+mbeans.size()+" MBeans");
      
      List mbeansCopy = new ArrayList(mbeans);
      ListIterator enum = mbeansCopy.listIterator();
      int serviceCounter = 0;
      while (enum.hasNext()) enum.next(); // pass them all
      while (enum.hasPrevious())
      {
         Service service = (Service) enum.previous();
         
         try
         {
            service.stop();
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not stop "+service, e);
         }
      }
      log.info("Stopped "+mbeansCopy.size()+" services");
   }
   
   public void destroy()
   {
      log.info("Destroying "+mbeans.size()+" MBeans");
      
      List mbeansCopy = new ArrayList(mbeans);
      ListIterator enum = mbeansCopy.listIterator();
      int serviceCounter = 0;
      while (enum.hasNext()) enum.next(); // pass them all
      while (enum.hasPrevious())
      {
         Service service = (Service) enum.previous();
         
         try
         {
            service.destroy();
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not destroy"+service, e);           
         }
      }
      log.info("Destroyed "+mbeansCopy.size()+" services");
   }

   public void register(Service service)
   {
       mbeans.add(service);
   }
   public void unregister(Service service)
   {
       mbeans.remove(service);
   }

   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      return name == null ? new ObjectName(OBJECT_NAME) : name;
   }
   
   public void postRegister(Boolean registrationDone)
   {
   }
   
   public void preDeregister()
      throws Exception
   {
   }
   
   public void postDeregister()
   {
   }

}


