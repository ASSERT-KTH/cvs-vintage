/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/

package org.jboss.ejb.plugins;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.Container;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.logging.Logger;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.monitor.Monitorable;
import org.jboss.monitor.client.BeanCacheSnapshot;
import org.jboss.monitor.MetricsConstants;
import org.jboss.util.CachePolicy;
import org.jboss.util.WorkerQueue;

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
 * @version $Revision: 1.37 $
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

   /* Helper class that handles synchronization for the passivation thread */
   private PassivationHelper m_passivationHelper;

   /* Flag for JMS monitoring of the cache */
   private boolean m_jmsMonitoring;

   /* Useful for log messages */
   private StringBuffer m_buffer = new StringBuffer();

   public void sample(Object s)
   {
      if( m_cache == null )
         return;

      synchronized (getCacheLock())
      {
         BeanCacheSnapshot snapshot = (BeanCacheSnapshot)s;
         snapshot.m_passivatingBeans = m_passivationHelper.m_passivationJobs.size();
         CachePolicy policy = getCache();
         if (policy instanceof Monitorable)
         {
            ((Monitorable)policy).sample(s);
         }
      }
   }
   
   public void retrieveStatistics( List container, boolean reset ) {
   }
   
   public void setJMSMonitoringEnabled(boolean enable) {m_jmsMonitoring = enable;}
   public boolean isJMSMonitoringEnabled() {return m_jmsMonitoring;}

   public EnterpriseContext get(Object id)
      throws RemoteException, NoSuchObjectException
   {
      if(id == null)
      {
         throw new IllegalArgumentException("Can't get an object with a null id");
      }

      EnterpriseContext ctx = null;
      synchronized (getCacheLock())
      {
         ctx = (EnterpriseContext)getCache().get(id);
         if (ctx == null)
         {
            // Here I block if the bean is passivating now
            ctx = unschedulePassivation(id);

            // A little defensive coding here.
            // ctx.getId() == null means that the ctx was ejbRemoved
            if (ctx != null && ctx.getId() == null)
            {
               log.warn("unschedulePassivation returned a passivated object with a null getId(), this ctx will NOT be reused");
               ctx = null;
            }
            // Already passivated ?
            if (ctx == null)
            {
               try
               {
                  ctx = acquireContext();
                  ctx.setId(id);
                  // FIXME marcf: in the case of entity the activation is a cheap call but
                  // in the case of stateful this a very expensive call (read from file)
                  // Locking on the whole cache lock?
                  activate(ctx);
                  logActivation(id);
                  insert(ctx);
               }
               catch (Exception x)
               {
                  if (ctx != null)
                     freeContext(ctx);
                  throw new NoSuchObjectException(x.getMessage());
               }
            }
            else
            {
               insert(ctx);
            }
         }
      }
      // FIXME marcf: How can this ever be reached? the ctx is always assigned
      if (ctx == null) throw new NoSuchObjectException("Can't find bean with id = " + id);

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
         if(cache.peek(id) == null)
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
         // This call, executed anyway, leaves door open to multiple scheduling
         // of the same context, which I take care in other places, in
         // PassivationHelper.schedule. I'm not sure that moving the call below
         // just after getCache().remove above would not lead to other
         // problems, so I leave it here.
         schedulePassivation(ctx);
      }
   }

   public void remove(Object id)
   {
      if(id == null)
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
         Constructor ctor = cls.getConstructor(new Class[] {AbstractInstanceCache.class});
         m_cache = (CachePolicy)ctor.newInstance(new Object[] {this});
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
               ((XmlLoadable)m_cache).importXml(policyConf);
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
      m_passivationHelper = new PassivationHelper();
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
         m_passivationHelper.clear();
      }
      this.m_cache = null;
      m_passivationHelper = null;
      m_buffer.setLength(0);
   }

   /**
    * Schedules the given EnterpriseContext for passivation
    * @see PassivationHelper#schedule
    */
   protected void schedulePassivation(EnterpriseContext ctx)
   {
      m_passivationHelper.schedule(ctx);
      logPassivationScheduled(ctx.getId());
   }

   /**
    * Tries to unschedule the given EnterpriseContext for passivation; returns
    * the unscheduled context if it wasn't passivated yet, null if the
    * passivation already happened.
    * @see PassivationHelper#unschedule
    */
   protected EnterpriseContext unschedulePassivation(Object id)
   {
      return m_passivationHelper.unschedule(id);
   }

   protected void logActivation(Object id)
   {
      if( log.isTraceEnabled() )
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
      if( log.isTraceEnabled() )
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
      if( log.isTraceEnabled() )
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
      if( log.isTraceEnabled() )
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
   protected CachePolicy getCache() {return m_cache;}

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

   /**
    * Helper class that schedules, unschedules, and executes the passivation jobs.
    */
   protected class PassivationHelper
   {
      /* The map that holds the passivation jobs posted */
      private Map m_passivationJobs;

      protected PassivationHelper()
      {
         m_passivationJobs = Collections.synchronizedMap(new HashMap());
      }

      protected void clear()
      {
         log.debug("Cancelling "+m_passivationJobs.size()+" passivation jobs");
         Iterator iter = m_passivationJobs.values().iterator();
         while( iter.hasNext() )
         {
            AbstractPassivationJob job = (AbstractPassivationJob) iter.next();
            job.cancel();
         }
         m_passivationJobs.clear();
      }

      /**
       * Creates and schedules a {@link PassivationJob} for passivation
       */
      protected void schedule(EnterpriseContext bean)
      {
         // Register only once the job to be able to unschedule its passivation
         Object id = bean.getId();
         if (m_passivationJobs.get(id) == null)
         {
            // (Bill Burke) We can't rely on the EnterpriseContext to provide PassivationJob
            // with a valid id because it may get freed to the InstancePool, then
            // reused before the PassivationJob executes.
            // marcf, actually for simplicity I have removed the "freed" call in the pool (check entity pool)
            AbstractPassivationJob job = new AbstractPassivationJob(bean, id)
               {
                  public void execute() throws Exception
                  {
                     // Validate that the container has not been destroyed
                     Container container = null;
                     BeanLock lock = null;
                     Object id = this.getKey();
                     synchronized (getCacheLock())
                     {
                        container = getContainer();
                        if( container != null )
                           lock = container.getLockManager().getLock(id);
                     }
                     if( container == null )
                        return;

                     if (ctx.getId() == null)
                     {
                        // If this happens, then a passivation request for this bean was issued
                        // but not yet executed, and in the meanwhile the bean has been removed.
                        return;
                     }

                     /**
                      * Synchronization / Passivation explanations:
                      * The instance interceptor (II) first acquires the Sync object associated
                      * with the given id, then asks to the instance cache (IC) for an enterprise
                      * context.
                      * The IC, if the context is not present, activates it and returns it.
                      * If the context is not in the IC then or it has already been
                      * passivated, or it is scheduled for passivation. If the latter is true,
                      * then the job is canceled and passivation does not occur, and the context
                      * is reinserted in the IC.
                      * This activity synchronizes in the following order the following objects:
                      * 1) the Sync object associated to the context's id via getLock(id)
                      * 2) the cache lock object via getCacheLock()
                      * 3) the passivation job, since cancel() is synchronized.
                      * To avoid deadlock, here we MUST acquire these resources in the same
                      * exact order.
                      *
                      * marcf: this is still very valid but the first lock is on the ctx directly
                      * this is part of the rework of the buzy wait bug
                      */
                     lock.sync();
                     ClassLoader cl = Thread.currentThread().getContextClassLoader();
                     ClassLoader beanCL = container.getClassLoader();
                     try
                     {
                        Thread.currentThread().setContextClassLoader(beanCL);
                        // marcf: the mutex is not good as we need a thread reentrant one, for now we
                        // use straight synchronization on the ctx as there is a one-one relationship
                        // Also the ctx are not reused any longer so no chance of using the wrong ctx
                        // mutex.acquire();

                        synchronized (getCacheLock())
                        {
                           // This is absolutely fundamental: the job must be removed from
                           // the map in every case. If it remains there, the call to
                           // PassivationHelper.unschedule() will cause the corrispondent
                           // context to be reinserted in the cache, and if then is passivated
                           // we have a context without meaning in the cache
                           m_passivationJobs.remove(id);

                           synchronized (this)
                           {
                              if (ctx instanceof EntityEnterpriseContext)
                              {
                                 // Verify that this ctx hasn't already
                                 // been freed and re-used by another thread.
                                 EntityEnterpriseContext entityCtx = (EntityEnterpriseContext) ctx;
                                 if(!entityCtx.getId().equals(id))
                                 {
                                    // ctx has been freed then re-used in another thread.
                                    entityCtx = null;
                                    return;
                                 }
                              }
                              // Can passivate checks that the ctx is not locked and that there is no
                              // transaction.
                              // The reason is that we can have a ctx scheduled for passivation
                              // but the moment we reach the passivation we can acquire all
                              // the temporary locks but there might be a running thread in the container
                              // (signified by the ctx.lock) and/or a long running transaction.
                              if (!canPassivate(ctx))
                              {
                                 // This check is done because there could have been
                                 // a request for passivation of this bean, but before
                                 // being passivated it got a request and has already
                                 // been inserted in the cache by the cache
                                 if (getCache().peek(id) == null)
                                 {
                                    getCache().insert(id, ctx);
                                 }

                                 logPassivationPostponed(id);

                                 return;
                              }
                              else
                              {
                                 if (!isCancelled())
                                 {
                                    try
                                    {
                                       // If the next call throws RemoteException we reinsert
                                       // the bean in the cache; every successive passivation
                                       // attempt will fail. The other policy would have been
                                       // to remove it, but then clients unexpectedly won't
                                       // find it anymore. On the other hand, on the server
                                       // log it is possible to see that passivation for the
                                       // bean failed, and fix it. See EJB 1.1, 6.4.1
                                       passivate(ctx);
                                       executed();
                                       freeContext(ctx);

                                       logPassivation(id);
                                    }
                                    catch (RemoteException x)
                                    {
                                       // Can't passivate this bean, reinsert it in the cache
                                       getCache().insert(id, ctx);
                                       throw x;
                                    }
                                 }
                              } // can passivate
                           } // synchronized(job)
                        }// synchronized(cacheLock)

                     }//synchronized(ctx)
                     finally
                     {
                        Thread.currentThread().setContextClassLoader(cl);
                        lock.releaseSync();
                     }
                  }//execute
               };// Passivation job definition

            // This method is entered only by one thread at a time, since the only caller
            // call it only after having sync on the cache lock via getCacheLock().
            // However, enforce this sync'ing on the passivation job's map
            synchronized (m_passivationJobs)
            {
               if (m_passivationJobs.get(id) == null)
               {
                  // Register job   
                   m_passivationJobs.put(id, job);   
    
                   // Schedule the job for passivation   
                   m_passivator.putJob(job);
               }
               else
               {
                  // Here is definitely a bug.
                  // This method should be accessed only from 1 point and one thread
                  // at a time; check why it isn't so.
                  throw new IllegalStateException("Trying to schedule 2 passivation jobs for the same bean");
               }
            }
         }
      }// schedule

      /**
       * Tries to unschedule a job paired with the given context's id
       * @return null if the bean has been passivated, the context
       * paired with the given id otherwise
       */
      protected EnterpriseContext unschedule(Object id)
      {
         // I chose not to remove canceled job here becauses multiple
         // unscheduling requests can arrive. This way all will be served

         // Is the passivation job for id still to be executed ?
         AbstractPassivationJob job = (AbstractPassivationJob)m_passivationJobs.get(id);
         if (job != null)
         {
            // Still to execute or executing now, cancel the job
            job.cancel();
            // Sync to not allow method execute to be executed after
            // the if statement below but before the return
            synchronized (job)
            {
               if (!job.isExecuted())
               {
                  // Still to be executed, return the bean
                  return job.getEnterpriseContext();
               }
            }
         }
         // Unscheduling request arrived too late, bean already passivated
         return null;
      }
   }
}
