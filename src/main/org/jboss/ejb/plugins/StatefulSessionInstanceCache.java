/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.rmi.RemoteException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * Cache for stateful session beans.
 *
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.28 $
 */
public class StatefulSessionInstanceCache extends AbstractInstanceCache
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /* The container */
   private StatefulSessionContainer m_container;

   /** The map<id, Long> that holds passivated bean ids that have been removed
    * from the cache and passivated to the pm along with the time of passivation
    */
   private ConcurrentReaderHashMap passivatedIDs = new ConcurrentReaderHashMap();

   /* Ids that are currently being activated */
   private HashSet activating = new HashSet();

   /* Used for logging */
   private StringBuffer buffer = new StringBuffer();

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   /** Get the passivated count.
    * @jmx:managed-attribute
    * @return the number of passivated instances.
    */
   public long getPassivatedCount()
   {
      return passivatedIDs.size();
   }

   /* From ContainerPlugin interface */
   public void setContainer(Container c)
   {
      m_container = (StatefulSessionContainer) c;
   }

   public void destroy()
   {
      synchronized (this)
      {
         this.m_container = null;
      }
      passivatedIDs.clear();
      super.destroy();
   }

   // Z implementation ----------------------------------------------

   // Y overrides ---------------------------------------------------

   protected synchronized Container getContainer()
   {
      return m_container;
   }

   protected void passivate(EnterpriseContext ctx) throws RemoteException
   {
      m_container.getPersistenceManager().passivateSession((StatefulSessionEnterpriseContext) ctx);
      passivatedIDs.put(ctx.getId(), new Long(System.currentTimeMillis()));
   }

   protected void activate(EnterpriseContext ctx) throws RemoteException
   {
      m_container.getPersistenceManager().activateSession((StatefulSessionEnterpriseContext) ctx);
      passivatedIDs.remove(ctx.getId());
   }
   
   protected boolean doActivate(EnterpriseContext ctx) throws RemoteException
   {
      Object id = ctx.getId();
      synchronized (activating)
      {
         // This is a recursive invocation
         if (activating.contains(id))
            return false;
         activating.add(id);
      }
      try
      {
         return super.doActivate(ctx);
      }
      finally
      {
         synchronized (activating)
         {
            activating.remove(id);
         }
      }
   }

   protected EnterpriseContext acquireContext() throws Exception
   {
      return m_container.getInstancePool().get();
   }

   protected void freeContext(EnterpriseContext ctx)
   {
      m_container.getInstancePool().free(ctx);
   }

   protected Object getKey(EnterpriseContext ctx)
   {
      return ctx.getId();
   }

   protected void setKey(Object id, EnterpriseContext ctx)
   {
      ctx.setId(id);
   }

   protected boolean canPassivate(EnterpriseContext ctx)
   {
      if (ctx.isLocked())
      {
         // The context is in the interceptor chain
         return false;
      }
      else if (m_container.getLockManager().canPassivate(ctx.getId()) == false)
      {
         return false;
      }
      else
      {
         if (ctx.getTransaction() != null)
         {
            try
            {
               return (ctx.getTransaction().getStatus() == Status.STATUS_NO_TRANSACTION);
            }
            catch (SystemException e)
            {
               // SA FIXME: not sure what to do here
               return false;
            }
         }
      }
      return true;
   }

   /** Remove all passivated instances that have been inactive too long.
    * @param maxLifeAfterPassivation the upper bound in milliseconds that an
    * inactive session will be kept.
    */
   protected void removePassivated(long maxLifeAfterPassivation)
   {
      StatefulSessionPersistenceManager store = m_container.getPersistenceManager();
      long now = System.currentTimeMillis();
      log.debug("removePassivated, now="+now+", maxLifeAfterPassivation="+maxLifeAfterPassivation);
      Iterator entries = passivatedIDs.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         Object key = entry.getKey();
         long passivationTime = ((Long) entry.getValue()).longValue();
         if (now - passivationTime > maxLifeAfterPassivation)
         {
            preRemovalPreparation(key);
            store.removePassivated(key);
            if (log.isTraceEnabled())
               log(key, passivationTime);
            // Must use iterator to avoid ConcurrentModificationException
            entries.remove();
            postRemovalCleanup(key);
         }
      }
   }

   // Protected -----------------------------------------------------

   protected void preRemovalPreparation(Object key)
   {
      //  no-op...extending classes may add prep
   }

   protected void postRemovalCleanup(Object key)
   {
      //  no-op...extending classes may add cleanup
   }

   // Private -------------------------------------------------------

   private void log(Object key, long passivationTime)
   {
      if (log.isTraceEnabled())
      {
         buffer.setLength(0);
         buffer.append("Removing from storage bean '");
         buffer.append(m_container.getBeanMetaData().getEjbName());
         buffer.append("' with id = ");
         buffer.append(key);
         buffer.append(", passivationTime=");
         buffer.append(passivationTime);
         log.trace(buffer.toString());
      }
   }

   // Inner classes -------------------------------------------------

}