/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.monitor;

import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.net.URL;

import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.JMException;

import org.jboss.ejb.EJBDeployer;
import org.jboss.ejb.EJBDeployerMBean;
import org.jboss.ejb.Application;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.logging.Logger;
import org.jboss.monitor.client.BeanCacheSnapshot;

/**
 *
 * @see Monitorable
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.7 $
 */
public class BeanCacheMonitor
   implements BeanCacheMonitorMBean, MBeanRegistration
{
   // Constants ----------------------------------------------------
   
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(BeanCacheMonitor.class);
   MBeanServer m_mbeanServer;
   // Static -------------------------------------------------------
   
   // Constructors -------------------------------------------------
   public BeanCacheMonitor()
   {}
   
   // Public -------------------------------------------------------
   
   // MBeanRegistration implementation -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   throws Exception
   {
      m_mbeanServer = server;
      return name;
   }
   
   public void postRegister(Boolean registrationDone)
   {}
   public void preDeregister() throws Exception
   {}
   public void postDeregister()
   {}
   
   // CacheMonitorMBean implementation -----------------------------------
   public BeanCacheSnapshot[] getSnapshots()
   {
      Iterator applications = null;
      // Get map of deployed applications
      try
      {
         applications = (Iterator)m_mbeanServer.invoke(EJBDeployerMBean.OBJECT_NAME, "getDeployedApplications", new Object[]
         {}, new String[]
         {});
      }
      catch (JMException x)
      {
         log.error("getDeployedApplications failed", x);
         return null;
      }
      
      ArrayList cacheSnapshots = new ArrayList();
      
      // For each application, getContainers()
      while (applications.hasNext())
      {
         Application app = (Application)applications.next();
         String name = app.getName();
         
         // Loop on each container of the application
         for (Iterator containers = app.getContainers().iterator(); containers.hasNext();)
         {
            // Get the cache for each container
            InstanceCache cache = null;
            Object container = containers.next();
            if (container instanceof EntityContainer)
            {
               cache = ((EntityContainer)container).getInstanceCache();
            }
            else if (container instanceof StatefulSessionContainer)
            {
               cache = ((StatefulSessionContainer)container).getInstanceCache();
            }
            
            // Take a cache snapshot
            if (cache instanceof Monitorable)
            {
               BeanCacheSnapshot snapshot = new BeanCacheSnapshot();
               snapshot.m_application = name;
               snapshot.m_container = ((Container)container).getBeanMetaData().getEjbName();
               ((Monitorable)cache).sample(snapshot);
               cacheSnapshots.add(snapshot);
            }
         }
      }
      
      return (BeanCacheSnapshot[])cacheSnapshots.toArray(new BeanCacheSnapshot[0]);
   }
   
   // Inner classes -------------------------------------------------
}

