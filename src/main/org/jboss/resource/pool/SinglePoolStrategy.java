/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource.pool;

import java.sql.SQLException;
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
 *   This strategy uses a single pool of managed connections; it is
 *   left up to the managed connection factory to select an
 *   appropriate connection. The efficiency of this strategy depends
 *   on how the resource adapter implements
 *   <code>matchManagedConnections</code>.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
class SinglePoolStrategy
   implements PoolStrategy
{
   // Attributes ----------------------------------------------------

   private ObjectPool pool;
   private ManagedConnectionFactory mcf;
   private Log log;
   private ConnectionEventListener listener;

   // These are used as communication from getManagedConnection to
   // MCPoolObjectFactory.createObject
   private boolean okToCreate = false;
   private Subject subject;
   private ConnectionRequestInfo cxRequestInfo;

   // Constructors --------------------------------------------------

   SinglePoolStrategy(ManagedConnectionFactory mcf, Log log)
   {
      this.log = log;
      this.mcf = mcf;
      pool = new ObjectPool(new MCPoolObjectFactory(), mcf.toString());
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
      ManagedConnection mc;
      synchronized (pool)
      {
         this.subject = subject;
         this.cxRequestInfo = cxRequestInfo;
         okToCreate = true;
         try
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
         finally
         {
            okToCreate = false;
            this.subject = null;
            this.cxRequestInfo = null;
         }
      }

      return mc;
   }

   public void releaseManagedConnection(ManagedConnection mc)
      throws ResourceException
   {
      synchronized (pool)
      {
         pool.releaseObject(mc);
      }
   }

   public void condemnManagedConnection(ManagedConnection mc)
   {
      synchronized (pool)
      {
         pool.markObjectAsInvalid(mc);
      }
   }

   public void shutdown()
   {
      synchronized (pool)
      {
         pool.shutDown();
      }
   }

   // Inner classes -------------------------------------------------

   private class MCPoolObjectFactory
      extends PoolObjectFactory
   {
      public Object createObject()
      {
         log.debug("Creating mamanged connection");
         try
         {
            synchronized (pool)
            {
               if (!okToCreate)
               {
                  // This means that the pool has taken it upon itself
                  // to create a connection. We can't do this because
                  // we don't know what to use for subject or
                  // cxRequestInfo.
                  throw new ApplicationServerInternalException(
                     "Pool attempted to create a connection not in response " +
                     "to getManagedConnection");
               }
               ManagedConnection mc =
                  mcf.createManagedConnection(subject, cxRequestInfo);
               return mc;
            }
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
