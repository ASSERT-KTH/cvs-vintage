/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.Collections;

import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

import org.jboss.ejb.deployment.jBossSession;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class NoPassivationStatefulSessionInstanceCache
   implements InstanceCache
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Container con;
   
   Map active = Collections.synchronizedMap(new HashMap());

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   /**
    *   Set the callback to the container. This is for initialization.
    *   The IM may extract the configuration from the container.
    *
    * @param   c  
    */
   public void setContainer(Container c)
   {
      this.con = c;
   }
   
   public void init()
      throws Exception
   {
   }
   
   public void start()
      throws Exception
   {
   }
   
   public void stop()
   {
   }

   public void destroy()
   {
   }
   
   public synchronized EnterpriseContext get(Object id)
      throws RemoteException
   {
      // TODO: minimize synchronization of IM
      
      StatefulSessionEnterpriseContext ctx;
      InstanceInfo info = null;
      while ((ctx = (StatefulSessionEnterpriseContext)active.get(id)) != null)
      {
         synchronized(ctx)
         {
            info = (InstanceInfo)ctx.getCacheContext();
            if (info.isLocked())
               throw new RemoteException("Concurrent call to stateful session is not allowed");
         }
      }
      
      if (ctx == null) // Not in cache
      {
         // Get new instance from pool
         ctx = (StatefulSessionEnterpriseContext)con.getInstancePool().get();
         
         // Activate
         ctx.setId(id);
         ((StatefulSessionContainer)con).getPersistenceManager().activateSession(ctx);
         insert(ctx);
      } else
      {
         // Lock the instance
         info.lock();
      }
      
      // At this point we own the instance with the given identity
//      System.out.println("Got entity:"+ctx.getId());
      return ctx;
   }

   public synchronized void insert(EnterpriseContext ctx)
   {
      InstanceInfo info = createInstanceInfo((StatefulSessionEnterpriseContext)ctx);
      ((StatefulSessionEnterpriseContext)ctx).setCacheContext(info);
      info.lock();
      active.put(ctx.getId(), ctx);
   }
   
   public void release(EnterpriseContext ctx)
   {
      // This context is now available for other threads
      synchronized(ctx)
      {
         ((InstanceInfo)((StatefulSessionEnterpriseContext)ctx).getCacheContext()).unlock();
//         System.out.println("Release entity:"+ctx.getId());
         if (!((InstanceInfo)((StatefulSessionEnterpriseContext)ctx).getCacheContext()).isLocked())
            ctx.notify();
      }
   }
   
   public synchronized void remove(Object id)
   {
      Object ctx = active.remove(id);
      synchronized(ctx)
      {
         ctx.notifyAll();
      }
   }
   
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected InstanceInfo createInstanceInfo(StatefulSessionEnterpriseContext ctx)
   {
      return new InstanceInfo(ctx);
   }
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   class InstanceInfo
   {
      int locked = 0; // 0 == unlocked, >0 == locked
      
      StatefulSessionEnterpriseContext ctx;
      
      InstanceInfo(StatefulSessionEnterpriseContext ctx)
      {
         this.ctx = ctx;
      }
      
      public void lock()
      {
         locked++;
      }
      
      public void unlock()
      {
         locked--;
      }
      
      public boolean isLocked()
      {
         return locked > 0;
      }
      
      public StatefulSessionEnterpriseContext getContext()
      {
         return ctx;
      }
   }
}
