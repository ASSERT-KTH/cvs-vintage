/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource.pool;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

import javax.security.auth.Subject;

import org.jboss.logging.Log;
import org.jboss.logging.LogWriter;
import org.jboss.minerva.pools.ObjectPool;
import org.jboss.minerva.pools.PoolEvent;
import org.jboss.minerva.pools.PoolObjectFactory;

/**
 *   This strategy uses many pools of managed connections, one for
 *   each security context used by the application. This should
 *   provide more efficient matching in the case when there are many
 *   different security contexts and the resource adapter does not
 *   support reauthentication. This will be the case, for example,
 *   when using JDBC with many different resource principals.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 *
 *   @see org.jboss.resource.pool.PoolObjectFactory
 */
class GroupBySecurityStrategy
   implements PoolStrategy
{
   // Attributes ----------------------------------------------------

   /** Maps from <code>SecurityContext</code> to <code>ObjectPool</code> */
   private Map pools = new HashMap();

   /** Maps from <code>ManagedConnection</code> to
       <code>ObjectPool</code> for identifying which pool a managed
       connectino was issued from. */
   private Map issuingPools = Collections.synchronizedMap(new HashMap());

   private ManagedConnectionFactory mcf;
   private Log log;
   private ConnectionEventListener listener;

   // Constructors --------------------------------------------------

   /**
    * Constructed by <code>PoolObjectFactory</code>.
    */
   GroupBySecurityStrategy(ManagedConnectionFactory mcf, Log log)
   {
      this.log = log;
      this.mcf = mcf;
   }

   // PoolStrategy implementation -----------------------------------

   public void setConnectionEventListener(ConnectionEventListener listener)
   {
      this.listener = listener;
   }

   public ManagedConnection getManagedConnection(
      Subject subject, ConnectionRequestInfo cxRequestInfo
      ) throws ResourceException
   {
      SecurityContext secCtx = new SecurityContext(subject, cxRequestInfo);

      // First, find the appropriate pool
      ObjectPool pool;

      // The synchronisation avoids a race if two threads both use the
      // same new security context. Shutdown is the only other time
      // pools is modified.
      synchronized (pools)
      {
         pool = (ObjectPool) pools.get(secCtx);
         if (pool == null)
         {
            log.debug("No pool for security context '" + secCtx + "', " +
                      "creating one");
            pool = createPool(secCtx);
            pools.put(secCtx, pool);
            // Note that nothing is removed from pools, so it can grow
            // and potentially run the system into the ground, but only
            // if there are a *very* large number of security contexts.
         }
      }

      // Then, find an appropriate managed connection from it
      ManagedConnection mc;
      synchronized (pool)
      {
         if (subject == null && cxRequestInfo == null)
         {
            // We don't bother the managed connection factory with
            // connection matching if there's no information for it
            // to use. This is actually a work-around for a NPE in
            // the black-box reference resource adapter.
            mc = (ManagedConnection) pool.getObject();
         }
         else
         {
            Set mcs = pool.getObjects();
            try
            {
               mc = mcf.matchManagedConnections(mcs, subject, cxRequestInfo);
               if (mc == null)
                  // This must return an appropriate managed
                  // connection because the pool is guaranteed to
                  // be empty after getObjects() so the pool will
                  // create a new managed connection using the
                  // correct subject and cxRequestInfo
                  mc = (ManagedConnection) pool.getObject();
               else
                  mcs.remove(mc);
            }
            finally
            {
               pool.releaseObjects(mcs);
            }
         }
      }

      issuingPools.put(mc, pool);
      return mc;
   }

   public void releaseManagedConnection(ManagedConnection mc)
      throws ResourceException
   {
      ObjectPool pool = (ObjectPool) issuingPools.remove(mc);
      if (pool == null)
         throw new ApplicationServerInternalException(
            "It appears that the managed connection '" + mc + "' was not " +
            "issued by this instance, so it can't be returned to its pool.");

      synchronized (pool)
      {
         pool.releaseObject(mc);
      }
   }

   public void condemnManagedConnection(ManagedConnection mc)
      throws ResourceException
   {
      ObjectPool pool = (ObjectPool) issuingPools.get(mc);
      if (pool == null)
         throw new ApplicationServerInternalException(
            "It appears that the managed connection '" + mc + "' was not " +
            "issued by this instance, so it can't be condemned.");
      synchronized (pool)
      {
         pool.markObjectAsInvalid(mc);
      }
   }

   public void shutdown()
   {
      if (issuingPools.size() > 0)
         log.warning("Pools being shut down with outstanding connections");
      synchronized (pools)
      {
         for (Iterator i=pools.values().iterator(); i.hasNext();)
         {
            ObjectPool pool = (ObjectPool) i.next();
            i.remove();
            synchronized (pool)
            {
               log.debug("Shutting down pool '" + pool + "'");
               pool.shutDown();
            }
         }
      }
   }

   // Private -------------------------------------------------------

   private ObjectPool createPool(SecurityContext secCtx)
   {
      ObjectPool pool = new ObjectPool(new MCPoolObjectFactory(secCtx),
                                       mcf.toString());
      try
      {
         pool.setLogWriter(new LogWriter(log));
      }
      catch (SQLException sqle)
      {
         log.exception(sqle);
      }
      //FIXME set the pool properties from the ConnectionFactoryLoader
      //(pass them in in some kind of metadata?)
      pool.initialize();
      return pool;
   }

   // Inner classes -------------------------------------------------

   private static class SecurityContext
   {
      public Subject subject;
      public ConnectionRequestInfo cxRequestInfo;

      public SecurityContext(Subject subject,
                             ConnectionRequestInfo cxRequestInfo)
      {
         this.subject = subject;
         this.cxRequestInfo = cxRequestInfo;
      }

      public boolean equals(Object o)
      {
         if (o instanceof SecurityContext)
         {
            SecurityContext other = (SecurityContext) o;
            boolean result = true;
            if (subject == null)
               result = result && other.subject == null;
            else
            {
               result = result && other.subject != null;
               result = result && subject.equals(other.subject);
            }
            if (cxRequestInfo == null)
               result = result && other.cxRequestInfo == null;
            else
            {
               result = result && other.cxRequestInfo != null;
               result = result && cxRequestInfo.equals(other.cxRequestInfo);
            }
            return result;
         }
         return false;
      }

      public int hashCode()
      {
         return ((subject==null) ? 0 : subject.hashCode()) ^
            ((cxRequestInfo==null) ? 0 : cxRequestInfo.hashCode());
      }

      public String toString()
      {
         return super.toString() + ": subject = \"" + subject + "\", " +
            "cxRequestInfo = \"" + cxRequestInfo + "\"";
      }
   }

   private class MCPoolObjectFactory
      extends PoolObjectFactory
   {
      private SecurityContext secCtx;

      public MCPoolObjectFactory(SecurityContext secCtx)
      {
         this.secCtx = secCtx;
      }

      public Object createObject()
      {
         log.debug("Creating mamanged connection");
         try
         {
            return mcf.createManagedConnection(secCtx.subject,
                                               secCtx.cxRequestInfo);
         }
         catch (ResourceException re)
         {
            log.exception(re);
            return null;
         }
      }

      public Object prepareObject(Object pooledObject)
      {
         ManagedConnection mc = (ManagedConnection) pooledObject;
         mc.addConnectionEventListener(listener);
         return mc;
      }

      public Object returnObject(Object pooledObject)
      {
         ManagedConnection mc = (ManagedConnection) pooledObject;
         try
         {
            mc.removeConnectionEventListener(listener);
            mc.cleanup();
            return mc;
         }
         catch (ResourceException re)
         {
            // Oh, no! What do we do now?
            log.exception(re);
            throw new RuntimeException(re.toString());
         }
      }

      public void deleteObject(Object pooledObject)
      {
         log.debug("Destroying mamanged connection '" + pooledObject + "'");
         ManagedConnection mc = (ManagedConnection) pooledObject;
         try
         {
            mc.destroy();
         }
         catch (ResourceException re)
         {
            log.exception(re);
         }
      }
   }
}
