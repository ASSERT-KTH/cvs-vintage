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
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.InstancePoolContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.deployment.jBossEntity;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.4 $
 */
public class NoPassivationEntityInstanceCache
   implements InstanceCache
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Container con;
   
   Map active = Collections.synchronizedMap(new HashMap());
   boolean isReentrant;

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
      isReentrant = ((jBossEntity)con.getMetaData()).getReentrant().equals("True");
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
      
      EntityEnterpriseContext ctx;
      InstanceInfo info = null;
      while ((ctx = (EntityEnterpriseContext)active.get(id)) != null)
      {
         synchronized(ctx)
         {
            info = (InstanceInfo)ctx.getCacheContext();
            if (!info.isLocked())
               break;
//            System.out.println("Cache is waiting for "+id+"("+info.isLocked()+","+ctx.getTransaction()+")");
               
            // Check if same tx; reentrant call
            try
            {
               if (ctx.getTransaction() != null && ctx.getTransaction().equals(con.getTransactionManager().getTransaction()))
               {
                  if (isReentrant)
                  {
                     break;
                  } else
                  {
                     throw new RemoteException("Reentrant call not allowed");
                  }
               }
            } catch (SystemException e)
            {
               throw new ServerException("Could not check for re-entrancy", e);
            }
               
            
            // Wait for previous tx to finish
            try 
            { 
               ctx.wait(5000); 
            } catch (InterruptedException e) 
            { 
               // Someone else is using this id right now
               throw new ServerException("Time out",e); 
            }
         }
      }
      
      if (ctx == null) // Not in cache
      {
         // Get new instance from pool
         ctx = (EntityEnterpriseContext)((InstancePoolContainer)con).getInstancePool().get();
         
         // Activate
         ctx.setId(id);
         ((EntityContainer)con).getPersistenceManager().activateEntity(ctx);
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
      InstanceInfo info = createInstanceInfo(ctx);
      ((EntityEnterpriseContext)ctx).setCacheContext(info);
      info.lock();
      active.put(ctx.getId(), ctx);
   }
   
   public void release(EnterpriseContext ctx)
   {
      // This context is now available for other threads
      synchronized(ctx)
      {
         ((InstanceInfo)((EntityEnterpriseContext)ctx).getCacheContext()).unlock();
//         System.out.println("Release entity:"+ctx.getId());
         if (!((InstanceInfo)((EntityEnterpriseContext)ctx).getCacheContext()).isLocked())
            ctx.notifyAll();
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
   protected InstanceInfo createInstanceInfo(EnterpriseContext ctx)
   {
      return new InstanceInfo(ctx);
   }
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   class InstanceInfo
   {
      int locked = 0; // 0 == unlocked, >0 == locked
      
      EntityEnterpriseContext ctx;
      
      InstanceInfo(EnterpriseContext ctx)
      {
         this.ctx = (EntityEnterpriseContext)ctx;
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
      
      public EntityEnterpriseContext getContext()
      {
         return ctx;
      }
   }
}
