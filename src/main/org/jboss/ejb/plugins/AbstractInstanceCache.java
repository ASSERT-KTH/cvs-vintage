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
import java.util.Map;
import java.util.HashMap;

import javax.ejb.EJBException;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

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
import org.jboss.util.Executable;
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
 * @version $Revision: 1.23 $
 *
 *   <p><b>Revisions:</b>
 *
 *   <p><b>20010703 marcf:</b>
 *   <ul>
 *   <li> Synchronization is on the context and not on the mutex any longer
 *   </ul>
 *   <p><b>20010704 marcf:</b>
 *   <ul>
 *   <li> Commented the getLock removeLock, temporarely, might need to come back when we go beyond
 *        locking at the context level and lock by ID
 *   </ul>
 * <p><b>2001/07/26: billb</b>
 * <ol>
 *   <li>Locking is now separate from EntityEnterpriseContext objects and is now
 *   encapsulated in BeanLock and BeanLockManager.  Did this because the lifetime
 *   of an EntityLock is sometimes longer than the lifetime of the ctx.
 * </ol>
 * <p><b>2001/08/07: billb</b>
 * <ol>
 *   <li>releaseLockRef should be enclosed in peek in remove()
 * </ol>
 */
public abstract class AbstractInstanceCache
   implements InstanceCache, XmlLoadable, Monitorable, MetricsConstants
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   protected static Logger log = Logger.getLogger(AbstractInstanceCache.class);
   /* The object that is delegated to implement the desired caching policy */
   private CachePolicy m_cache;
   /* The worker queue that passivates beans in another thread */
   private WorkerQueue m_passivator;
   /* The mutex object for the cache */
   private Object m_cacheLock = new Object();
   /* Helper class that handles synchronization for the passivation thread */
   private PassivationHelper m_passivationHelper;
   /* Flag for JMS monitoring of the cache */
   private boolean m_jmsMonitoring;
   /* Useful for log messages */
   private StringBuffer m_buffer = new StringBuffer();
   /* JMS log members */
   private TopicConnection m_jmsConnection;
   private Topic m_jmsTopic;
   private TopicSession m_jmsSession;
   private TopicPublisher m_jmsPublisher;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Monitorable implementation ------------------------------------
   public void sample(Object s)
   {
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
   public Map retrieveStatistic()
   {
      return null;
   }
   public void resetStatistic()
   {
   }

   // Public --------------------------------------------------------
   public void setJMSMonitoringEnabled(boolean enable) {m_jmsMonitoring = enable;}
   public boolean isJMSMonitoringEnabled() {return m_jmsMonitoring;}

   public void sendMessage(Message message)
   {
      try
      {
         message.setJMSType(BEANCACHE_METRICS);
         message.setJMSExpiration(5000);
         message.setLongProperty(TIME, System.currentTimeMillis());
         m_jmsPublisher.publish(m_jmsTopic, message);
      }
      catch (JMSException x)
      {
         log.error("sendMessage failed", x);
      }
   }
   public Message createMessage(Object id)
   {
      Message message = null;
      try
      {
         message = m_jmsSession.createMessage();
         message.setStringProperty(APPLICATION, getContainer().getApplication().getName());
         message.setStringProperty(BEAN, getContainer().getBeanMetaData().getEjbName());
         if (id != null)
         {
            message.setStringProperty(PRIMARY_KEY, id.toString());
         }
      }
      catch (JMSException x)
      {
         log.error("createMessage failed", x);
      }
      return message;
   }

   /* From InstanceCache interface */
   public EnterpriseContext get(Object id)
      throws RemoteException, NoSuchObjectException
   {
      if (id == null) throw new IllegalArgumentException("Can't get an object with a null key");

      EnterpriseContext ctx = null;

      synchronized (getCacheLock())
      {
         ctx = (EnterpriseContext)getCache().get(id);
         if (ctx == null)
         {
            // Here I block if the bean is passivating now
            ctx = unschedulePassivation(id);

            // Already passivated ?
            if (ctx == null)
            {
               try
               {
                  ctx = acquireContext();
                  setKey(id, ctx);
                  // FIXME marcf: in the case of entity the activation is a cheap call but
                  // in the case of stateful this a very expensive call (read from file)
                  // Locking on the whole cache lock?
                  activate(ctx);
                  logActivation(id);
                  insert(ctx);
               }
               catch (Exception x)
               {
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
   /* From InstanceCache interface */
   public void insert(EnterpriseContext ctx)
   {
      if (ctx == null) throw new IllegalArgumentException("Can't insert a null object in the cache");

      CachePolicy cache = getCache();
      synchronized (getCacheLock())
      {
         // This call must be inside the sync block, otherwise can happen that I get the
         // key, then the context is passivated and I will insert in cache a meaningless
         // context.
         Object key = getKey(ctx);

         if (cache.peek(key) == null)
         {
            cache.insert(key, ctx);
         }
         else
         {
            // Here it is a bug.
            // Check for all places where insert is called, and ensure that they cannot
            // run without having acquired the cache lock via getCacheLock()
            throw new IllegalStateException("INSERTING AN ALREADY EXISTING BEAN, ID = " + key);
         }
         //Create a lock in the lock manager, as long as there an instance in cache we keep
         // the lock in the manager, since get will increase the ref count on the lock
         getContainer().getLockManager().getLock(key);
      }
   }
   /* From InstanceCache interface */
   public void release(EnterpriseContext ctx)
   {
      if (ctx == null) throw new IllegalArgumentException("Can't release a null object");

      // Here I remove the bean; call to remove(id) is wrong
      // cause will remove also the cache lock that is needed
      // by the passivation, that eventually will remove it.
      synchronized (getCacheLock())
      {
         Object id = getKey(ctx);
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
   /* From InstanceCache interface */
   public void remove(Object id)
   {
      if (id == null) throw new IllegalArgumentException("Can't remove an object using a null key");

      synchronized (getCacheLock())
      {
         if (getCache().peek(id) != null)
         {
            getCache().remove(id);
            // When we introduced the bean in the cache we used the get to increase the lock count
            // now we decrease it and if the count is zero the manager will release the object from memory
            getContainer().getLockManager().removeLockRef(id);
         }
      }
   }

   public boolean isActive(Object id)
   {
      // Check whether an object with the given id is available in the cache
      return getCache().peek(id) != null;
   }
   // XmlLoadable implementation ----------------------------------------------
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

   /* From Service interface*/
   public void init() throws Exception
   {
      getCache().init();
      m_passivationHelper = new PassivationHelper();
      String threadName = "Passivator Thread for " + getContainer().getBeanMetaData().getEjbName();
      ClassLoader cl = getContainer().getClassLoader();
      m_passivator = new PassivatorQueue(threadName, cl);

      if (isJMSMonitoringEnabled())
      {
         // Setup JMS for cache monitoring
         Context namingContext = new InitialContext();
         Object factoryRef = namingContext.lookup("TopicConnectionFactory");
         TopicConnectionFactory factory = (TopicConnectionFactory)PortableRemoteObject.narrow(factoryRef, TopicConnectionFactory.class);

         m_jmsConnection = factory.createTopicConnection();

         Object topicRef = namingContext.lookup("topic/metrics");
         m_jmsTopic = (Topic)PortableRemoteObject.narrow(topicRef, Topic.class);
         m_jmsSession = m_jmsConnection.createTopicSession(false, Session.DUPS_OK_ACKNOWLEDGE);
         m_jmsPublisher = m_jmsSession.createPublisher(m_jmsTopic);
      }
   }
   /* From Service interface*/
   public void start() throws Exception
   {
      getCache().start();
      m_passivator.start();

      if (isJMSMonitoringEnabled())
      {
         m_jmsConnection.start();
      }
   }
   /* From Service interface*/
   public void stop()
   {
      // Empty the cache
      synchronized (getCacheLock())
      {
         getCache().stop();
      }

      if (m_passivator != null) {m_passivator.stop();}

      if (isJMSMonitoringEnabled() && m_jmsConnection != null)
      {
         try
         {
            m_jmsConnection.stop();
         }
         catch (JMSException ignored) {}
      }
   }
   /* From Service interface*/
   public void destroy()
   {
      getCache().destroy();
      if (isJMSMonitoringEnabled() && m_jmsConnection != null)
      {
         try
         {
            m_jmsConnection.close();
         }
         catch (JMSException ignored) {}
      }
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   /**
    * Schedules the given EnterpriseContext for passivation
    * @see PassivationHelper#schedule
    */
   protected void schedulePassivation(EnterpriseContext ctx)
   {
      m_passivationHelper.schedule(ctx);
      logPassivationScheduled(getKey(ctx));
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
      m_buffer.setLength(0);
      m_buffer.append("Activated bean ");
      m_buffer.append(getContainer().getBeanMetaData().getEjbName());
      m_buffer.append(" with id = ");
      m_buffer.append(id);
      log.debug(m_buffer.toString());

      if (isJMSMonitoringEnabled())
      {
         // Prepare JMS message
         Message message = createMessage(id);
         try
         {
            message.setStringProperty(TYPE, "ACTIVATION");
         }
         catch (JMSException x)
         {
            log.error("createMessage failed", x);
         }

         // Send JMS Message
         sendMessage(message);
      }
   }

   protected void logPassivationScheduled(Object id)
   {
      m_buffer.setLength(0);
      m_buffer.append("Scheduled passivation of bean ");
      m_buffer.append(getContainer().getBeanMetaData().getEjbName());
      m_buffer.append(" with id = ");
      m_buffer.append(id);
      log.debug(m_buffer.toString());

      if (isJMSMonitoringEnabled())
      {
         // Prepare JMS message
         Message message = createMessage(id);
         try
         {
            message.setStringProperty(TYPE, "PASSIVATION");
            message.setStringProperty(ACTIVITY, "SCHEDULED");
         }
         catch (JMSException x)
         {
            log.error("createMessage failed", x);
         }

         // Send JMS Message
         sendMessage(message);
      }
   }

   protected void logPassivation(Object id)
   {
      m_buffer.setLength(0);
      m_buffer.append("Passivated bean ");
      m_buffer.append(getContainer().getBeanMetaData().getEjbName());
      m_buffer.append(" with id = ");
      m_buffer.append(id);
      log.debug(m_buffer.toString());

      if (isJMSMonitoringEnabled())
      {
         // Prepare JMS message
         Message message = createMessage(id);
         try
         {
            message.setStringProperty(TYPE, "PASSIVATION");
            message.setStringProperty(ACTIVITY, "PASSIVATED");
         }
         catch (JMSException x)
         {
            log.error("createMessage failed", x);
         }

         // Send JMS Message
         sendMessage(message);
      }
   }

   protected void logPassivationPostponed(Object id)
   {
      m_buffer.setLength(0);
      m_buffer.append("Postponed passivation of bean ");
      m_buffer.append(getContainer().getBeanMetaData().getEjbName());
      m_buffer.append(" with id = ");
      m_buffer.append(id);
      log.debug(m_buffer.toString());

      if (isJMSMonitoringEnabled())
      {
         // Prepare JMS message
         Message message = createMessage(id);
         try
         {
            message.setStringProperty(TYPE, "PASSIVATION");
            message.setStringProperty(ACTIVITY, "POSTPONED");
         }
         catch (JMSException x)
         {
            log.error("createMessage failed", x);
         }

         // Send JMS Message
         sendMessage(message);
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
   protected abstract void passivate(EnterpriseContext ctx) throws RemoteException;
   /**
    * Activates the given EnterpriseContext
    */
   protected abstract void activate(EnterpriseContext ctx) throws RemoteException;
   /**
    * Acquires an EnterpriseContext from the pool
    */
   protected abstract EnterpriseContext acquireContext() throws Exception;
   /**
    * Frees the given EnterpriseContext to the pool
    */
   protected abstract void freeContext(EnterpriseContext ctx);
   /**
    * Returns the key used by the cache to map the given context
    */
   protected abstract Object getKey(EnterpriseContext ctx);
   /**
    * Sets the given id as key for the given context
    */
   protected abstract void setKey(Object id, EnterpriseContext ctx);
   /**
    * Returns whether the given context can be passivated or not
    *
    */
   protected abstract boolean canPassivate(EnterpriseContext ctx);

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
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

      /**
       * Creates and schedules a {@link PassivationJob} for passivation
       */
      protected void schedule(EnterpriseContext bean)
      {
         // Register only once the job to be able to unschedule its passivation
         Object key = getKey(bean);
         if (m_passivationJobs.get(key) == null)
         {
            // (Bill Burke) We can't rely on the EnterpriseContext to provide PassivationJob
            // with a valid key because it may get freed to the InstancePool, then
            // reused before the PassivationJob executes.
            // marcf, actually for simplicity I have removed the "freed" call in the pool (check entity pool)
            PassivationJob job = new PassivationJob(bean, key)
               {
                  public void execute() throws Exception
                  {
                     EnterpriseContext ctx = this.getEnterpriseContext();
                     if (ctx.getId() == null)
                     {
                        // If this happens, then a passivation request for this bean was issued
                        // but not yet executed, and in the meanwhile the bean has been removed.
                        return;
                     }

                     Object id = this.getKey();

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
                     BeanLock lock = getContainer().getLockManager().getLock(id);
                     lock.sync();
                     try {

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
                                 if (!((EntityEnterpriseContext)ctx).getCacheKey().equals(id))
                                 {
                                    // ctx has been freed then re-used in another thread.
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
                        lock.releaseSync();
                        getContainer().getLockManager().removeLockRef(id);
                     }
                  }//execute
               };// Passivation job definition

            // This method is entered only by one thread at a time, since the only caller
            // call it only after having sync on the cache lock via getCacheLock().
            // However, enforce this sync'ing on the passivation job's map
            synchronized (m_passivationJobs)
            {
               if (m_passivationJobs.get(key) == null)
               {
                  // Register job
                  m_passivationJobs.put(key, job);

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
         PassivationJob job = (PassivationJob)m_passivationJobs.get(id);
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

/**
 * Abstract class for passivation jobs.
 * Subclasses should implement {@link #execute} synchronizing it in some way because
 * the execute method is normally called in the passivation thread,
 * while the cancel method is normally called from another thread.
 * To avoid that subclasses override methods of this class without
 * make them synchronized (except execute of course), they're declared final.
 */
abstract class PassivationJob implements Executable
{
   private EnterpriseContext m_context;
   private Object m_key;
   private boolean m_cancelled;
   private boolean m_executed;

   PassivationJob(EnterpriseContext ctx, Object key)
   {
      m_context = ctx;
      m_key = key;
   }

   public abstract void execute() throws Exception;

   /**
    * (Bill Burke) We can't rely on the EnterpriseContext to provide PassivationJob
    * with a valid key because it may get freed to the InstancePool, then
    * reused before the PassivationJob executes.
    */
   final Object getKey()
   {
      return m_key;
   }
   /**
    * Returns the EnterpriseContext associated with this passivation job,
    * so the bean that will be passivated.
    * No need to synchronize access to this method, since the returned
    * reference is immutable
    */
   final EnterpriseContext getEnterpriseContext()
   {
      return m_context;
   }
   /**
    * Mark this job for cancellation.
    * @see #isCancelled
    */
   final synchronized void cancel()
   {
      m_cancelled = true;
   }
   /**
    * Returns whether this job has been marked for cancellation
    * @see #cancel
    */
   final synchronized boolean isCancelled()
   {
      return m_cancelled;
   }
   /**
    * Mark this job as executed
    * @see #isExecuted
    */
   final synchronized void executed()
   {
      m_executed = true;
   }
   /**
    * Returns whether this job has been executed
    * @see #executed
    */
   final synchronized boolean isExecuted()
   {
      return m_executed;
   }
}

class PassivatorQueue extends WorkerQueue
{
   protected static Logger log = Logger.getLogger(PassivatorQueue.class);
   /**
    * Used for debug purposes, holds the scheduled passivation jobs
    */
   // private Map m_map = new HashMap();

   /**
    * Creates a new passivator queue with default thread name of
    * "Passivator Thread".
    */
   PassivatorQueue()
   {
      this("Passivator Thread", null);
   }
   /**
    * Creates a new passivator queue with the given thread name and given
    * context class loader. <br>
    * @param threadName the name of the passivator thread
    * @param cl the context class loader; if null the context class loader is not set.
    */
   PassivatorQueue(String threadName, ClassLoader cl)
   {
      super(threadName);
      if (cl != null)
      {
         m_queueThread.setContextClassLoader(cl);
      }
   }
   /**
 * Overridden for debug purposes
 *//*
 protected Executable getJobImpl() throws InterruptedException
 {
 PassivationJob j = (PassivationJob)super.getJobImpl();
 EnterpriseContext ctx = j.getEnterpriseContext();
 Object id = ctx.getId();
 m_map.remove(id);
 return j;
 }
 */
 /**
 * Overridden for debug purposes
 *//*
 protected void putJobImpl(Executable job)
 {
 PassivationJob j = (PassivationJob)job;
 EnterpriseContext ctx = j.getEnterpriseContext();
 Object id = ctx.getId();
 if (m_map.get(id) != null)
 {
 // Here is a bug, job requests are scheduled only once per bean.
 System.err.println("DUPLICATE PASSIVATION JOB INSERTION FOR ID = " + ctx.getId());
 System.err.println("CTX isLocked: " + ctx.isLocked());
 System.err.println("CTX transaction: " + ctx.getTransaction());
 throw new IllegalStateException();
 }
 else
 {
 m_map.put(id, job);
 }
 super.putJobImpl(job);
 }
 */
 /**
 * Logs exceptions thrown during job execution.
 */
   protected void logJobException(Exception x)
   {
      // Log system exceptions
      if (x instanceof EJBException)
      {
         Exception nestedX = ((EJBException)x).getCausedByException();
         if (nestedX != null)
         {
            log.error("BEAN EXCEPTION", x);
         }
      } else {
         log.error("EXCEPTION", x);
      }
   }
}
