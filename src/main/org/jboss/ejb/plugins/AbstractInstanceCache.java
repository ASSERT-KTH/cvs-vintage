/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.ejb.plugins;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.logging.Logger;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.monitor.MetricsConstants;
import org.jboss.monitor.Monitorable;
import org.jboss.monitor.client.BeanCacheSnapshot;
import org.jboss.util.CachePolicy;
import org.jboss.util.WorkerQueue;
import org.w3c.dom.Element;

import java.lang.reflect.Constructor;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Base class for caches of entity and stateful beans. <p>
 * It manages the cache entries through a {@link CachePolicy} object;
 * the implementation of the cache policy object must respect the following
 * requirements:
 * <ul>
 * <li> Have a public constructor that takes a single argument of type
 * AbstractInstanceCache.class or a subclass
 * </ul>
 *
 *
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 *
 * @version $Revision: 1.38 $
 */
public abstract class AbstractInstanceCache
        implements InstanceCache, XmlLoadable, Monitorable, MetricsConstants
{
   /*
    * The worker queue that passivates beans in another thread. One passivator
    *is shared for all EJB deployments.
    */
   static private WorkerQueue m_passivator = new WorkerQueue("EJB Passivator Thread", true);

   static
   {
      // Start the passivator thread
      m_passivator.start();
   }

   protected static Logger log = Logger.getLogger(AbstractInstanceCache.class);

   /* The object that is delegated to implement the desired caching policy */
   private CachePolicy m_cache;

   /* The mutex object for the cache */
   private Object m_cacheLock = new Object();

   /* Flag for JMS monitoring of the cache */
   private boolean m_jmsMonitoring;

   /* Useful for log messages */
   private StringBuffer m_buffer = new StringBuffer();

   public void sample(Object s)
   {
      if (m_cache == null)
         return;

      synchronized (getCacheLock())
      {
         BeanCacheSnapshot snapshot = (BeanCacheSnapshot) s;
         snapshot.m_passivatingBeans = 0; // TODO resolve this
         CachePolicy policy = getCache();
         if (policy instanceof Monitorable)
         {
            ((Monitorable) policy).sample(s);
         }
      }
   }

   public void retrieveStatistics(List container, boolean reset)
   {
   }

   public void setJMSMonitoringEnabled(boolean enable)
   {
      m_jmsMonitoring = enable;
   }

   public boolean isJMSMonitoringEnabled()
   {
      return m_jmsMonitoring;
   }

   public EnterpriseContext get(Object id)
           throws RemoteException, NoSuchObjectException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("Can't get an object with a null id");
      }

      EnterpriseContext ctx = null;
      synchronized (getCacheLock())
      {
         CachePolicy cache = getCache();
         ctx = (EnterpriseContext) cache.get(id);
         if (ctx == null)
         {
            try
            {
               ctx = acquireContext();
               ctx.setId(id);
               activate(ctx);
               logActivation(id);
               insert(ctx);
            }
            catch (Exception x)
            {
               if (ctx != null)
                  freeContext(ctx);
               log.error("failed to create ctx", x);
               throw new NoSuchObjectException(x.getMessage());
            }
         }
      }
      return ctx;
   }

   public void insert(EnterpriseContext ctx)
   {
      if (ctx == null) throw new IllegalArgumentException("Can't insert a null object in the cache");

      CachePolicy cache = getCache();
      synchronized (getCacheLock())
      {
         // This call must be inside the sync block, otherwise can happen that I get the
         // id, then the context is passivated and I will insert in cache a meaningless
         // context.
         Object id = ctx.getId();
         if (cache.peek(id) == null)
         {
            cache.insert(id, ctx);
         }
         else
         {
            // Here it is a bug.
            // Check for all places where insert is called, and ensure that they cannot
            // run without having acquired the cache lock via getCacheLock()
            throw new IllegalStateException("INSERTING AN ALREADY EXISTING BEAN, ID = " + id);
         }
      }
   }

   public void release(EnterpriseContext ctx)
   {
      if (ctx == null) throw new IllegalArgumentException("Can't release a null object");

      // Here I remove the bean; call to remove(id) is wrong
      // cause will remove also the cache lock that is needed
      // by the passivation, that eventually will remove it.
      synchronized (getCacheLock())
      {
         Object id = ctx.getId();
         if (getCache().peek(id) != null)
         {
            getCache().remove(id);
         }
      }
      tryToPassivate(ctx);
   }


   public void remove(Object id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("Can't remove an object using a null id");
      }
      synchronized (getCacheLock())
      {
         if (getCache().peek(id) != null)
         {
            getCache().remove(id);
         }
      }
   }

   public boolean isActive(Object id)
   {
      // Check whether an object with the given id is available in the cache
      return getCache().peek(id) != null;
   }

   public void importXml(Element element) throws DeploymentException
   {
      // This one is mandatory
      String p = MetaData.getElementContent(MetaData.getUniqueChild(element, "cache-policy"));
      try
      {
         Class cls = Thread.currentThread().getContextClassLoader().loadClass(p);
         Constructor ctor = cls.getConstructor(new Class[]{AbstractInstanceCache.class});
         m_cache = (CachePolicy) ctor.newInstance(new Object[]{this});
      }
      catch (Exception x)
      {
         throw new DeploymentException("Can't create cache policy", x);
      }

      Element policyConf = MetaData.getOptionalChild(element, "cache-policy-conf");
      if (policyConf != null)
      {
         if (m_cache instanceof XmlLoadable)
         {
            try
            {
               ((XmlLoadable) m_cache).importXml(policyConf);
            }
            catch (Exception x)
            {
               throw new DeploymentException("Can't import policy configuration", x);
            }
         }
      }
   }

   public void create() throws Exception
   {
      getCache().create();
   }

   public void start() throws Exception
   {
      getCache().start();
   }

   public void stop()
   {
      // Empty the cache
      synchronized (getCacheLock())
      {
         getCache().stop();
      }
   }

   public void destroy()
   {
      synchronized (getCacheLock())
      {
         getCache().destroy();
      }
      this.m_cache = null;
      m_buffer.setLength(0);
   }

   protected void tryToPassivate(EnterpriseContext ctx)
   {
      Object id = ctx.getId();
      if (id == null) return;
      BeanLock lock = getContainer().getLockManager().getLock(id);
      try
      {
         lock.sync();
         try
         {
            if (canPassivate(ctx))
            {
               try
               {
                  passivate(ctx);
                  freeContext(ctx);
               }
               catch (Exception ignored)
               {
                  log.warn("failed to passivate, id=" + id, ignored);
               }
            }
            else
            {
               log.warn("Unable to passivate due to ctx lock, id=" + id);
            }
         }
         finally
         {
            lock.releaseSync();
         }
      }
      catch (InterruptedException e)
      {
      }
   }

   protected void logActivation(Object id)
   {
      if (log.isTraceEnabled())
      {
         m_buffer.setLength(0);
         m_buffer.append("Activated bean ");
         m_buffer.append(getContainer().getBeanMetaData().getEjbName());
         m_buffer.append(" with id = ");
         m_buffer.append(id);
         log.trace(m_buffer.toString());
      }
   }

   protected void logPassivationScheduled(Object id)
   {
      if (log.isTraceEnabled())
      {
         m_buffer.setLength(0);
         m_buffer.append("Scheduled passivation of bean ");
         m_buffer.append(getContainer().getBeanMetaData().getEjbName());
         m_buffer.append(" with id = ");
         m_buffer.append(id);
         log.trace(m_buffer.toString());
      }
   }

   protected void logPassivation(Object id)
   {
      if (log.isTraceEnabled())
      {
         m_buffer.setLength(0);
         m_buffer.append("Passivated bean ");
         m_buffer.append(getContainer().getBeanMetaData().getEjbName());
         m_buffer.append(" with id = ");
         m_buffer.append(id);
         log.trace(m_buffer.toString());
      }
   }

   protected void logPassivationPostponed(Object id)
   {
      if (log.isTraceEnabled())
      {
         m_buffer.setLength(0);
         m_buffer.append("Postponed passivation of bean ");
         m_buffer.append(getContainer().getBeanMetaData().getEjbName());
         m_buffer.append(" with id = ");
         m_buffer.append(id);
         log.trace(m_buffer.toString());
      }
   }

   /**
    * Returns the container for this cache.
    */
   protected abstract Container getContainer();

   /**
    * Returns the cache policy used for this cache.
    */
   protected CachePolicy getCache()
   {
      return m_cache;
   }

   /**
    * Returns the mutex used to sync access to the cache policy object
    */
   public Object getCacheLock()
   {
      return m_cacheLock;
   }

   /**
    * Passivates the given EnterpriseContext
    */
   protected abstract void passivate(EnterpriseContext ctx) throws Exception;

   /**
    * Activates the given EnterpriseContext
    */
   protected abstract void activate(EnterpriseContext ctx) throws Exception;

   /**
    * Acquires an EnterpriseContext from the pool
    */
   protected abstract EnterpriseContext acquireContext() throws Exception;

   /**
    * Frees the given EnterpriseContext to the pool
    */
   protected abstract void freeContext(EnterpriseContext ctx);

   /**
    * Returns whether the given context can be passivated or not
    *
    */
   protected abstract boolean canPassivate(EnterpriseContext ctx);

}
