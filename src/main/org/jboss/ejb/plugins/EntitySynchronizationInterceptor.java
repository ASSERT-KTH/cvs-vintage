/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.RemoveException;
import javax.ejb.EntityBean;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;

import org.jboss.logging.Logger;

/**
 *   This container filter takes care of EntityBean persistance.
 *   Specifically, it calls ejbStore at appropriate times
 *
 *   Possible options:
 *   After each call
 *   On tx commit
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class EntitySynchronizationInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
   public static final int TIMEOUT = 10000;
	
	// Commit time options
   public static final int A = 0; // Keep instance cached
   public static final int B = 1; // Invalidate state
   public static final int C = 2; // Passivate
    
   // Attributes ----------------------------------------------------
   HashMap synchs = new HashMap();  // tx -> synch
	
	int commitOption = A;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
	public void setCommitOption(int commitOption)
	{
		this.commitOption = commitOption;
	}
	
	public int getCommitOption()
	{
		return commitOption;
	}
	
   public void register(EnterpriseContext ctx, Transaction tx)
   {
      // Associate ctx with tx
      synchronized (synchs)
      {
         // Get synchronization for this tx - create if there is none
         InstanceSynchronization synch = (InstanceSynchronization)synchs.get(tx);
         if (synch == null)
         {
            // Register new synch with current tx
            synch = new InstanceSynchronization(tx);
            synchs.put(tx, synch);
            
            try
            {
               tx.registerSynchronization(synch);
            } catch (Exception e)
            {
               throw new EJBException(e);
            }
         }
         
         synch.add(ctx);
         ctx.setTransaction(tx);
      }
   }

   public void deregister(EntityEnterpriseContext ctx, Transaction tx)
   {
      // Deassociate ctx with tx
      synchronized (synchs)
      {
         // Get synchronization for this tx
         InstanceSynchronization synch = (InstanceSynchronization)synchs.get(tx);
         if (synch == null)
         {
            return;
         }
         
         synch.remove(ctx);
         ctx.setTransaction(null);
         ctx.setInvoked(false);
      }
   }
   
   // Interceptor implementation --------------------------------------
   
   public Object invokeHome(Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      try
      {
         return getNext().invokeHome(method, args, ctx);
      } finally
      {
         // Anonymous was sent in, so if it has an id it was created
         if (ctx.getId() != null && getContainer().getTransactionManager().getTransaction().getStatus() == Status.STATUS_ACTIVE)
         {
            // Set tx
            register(ctx, getContainer().getTransactionManager().getTransaction());
            
            // Currently synched with underlying storage
            ((EntityEnterpriseContext)ctx).setSynchronized(true);
         }
      }
   }

   public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      EntityEnterpriseContext entityCtx = (EntityEnterpriseContext)ctx;
      
      
      Transaction tx = ctx.getTransaction();
      Transaction current = getContainer().getTransactionManager().getTransaction();
      
//DEBUG      Logger.debug("TX:"+(current.getStatus() == Status.STATUS_ACTIVE));
      
      if (current.getStatus() == Status.STATUS_ACTIVE)
      {
         // Registered with other tx?
         if (tx != null && !tx.equals(current))
         {
            // Wait for other tx associated with ctx to finish
            while ((tx = ctx.getTransaction()) != null)
            {
               // Release context temporarily
               ((EntityContainer)getContainer()).getInstanceCache().release(ctx);
               
               // Wait for tx to end
//DEBUG               Logger.debug("Wait for "+ctx.getId()+":Current="+current+", Tx="+tx);
               Synchronization synch = new Synchronization()
               {
                  public void beforeCompletion() {}
                  public synchronized void afterCompletion(int status)
                  {
                     this.notifyAll();
                  }
               };
               tx.registerSynchronization(synch);
               synchronized(synch)
               {
                  try
                  {
                     tx.wait(TIMEOUT); // This should be changed to a s
                  } catch (InterruptedException e)
                  {
                     throw new ServerException("Time out", e);
                  }
               }
               
               // Get context again -- it may be gone by now though if other tx removed it
               ctx = ((EntityContainer)getContainer()).getInstanceCache().get(ctx.getId());
            }
            
         } 
         
         // At this point the ctx is either not associated with a tx, 
         // or it has been previously associated with the current tx
         
         // Synchronize with DB
//DEBUG         Logger.debug("SYNCH");
         if (!entityCtx.isSynchronized())
         {
            ((EntityContainer)getContainer()).getPersistenceManager().loadEntity(entityCtx);
            entityCtx.setSynchronized(true);
         }
         
         try
         {
            return getNext().invoke(id, method, args, ctx);
         } finally
         {
            if (ctx.getId() != null)
            {
               // Associate ctx with tx
               if (tx == null)
               {
                  entityCtx.setInvoked(true); // This causes ejbStore to be invoked on tx commit
                  register(ctx, current);
               }
            } else
            {
               // Entity was removed
               if (ctx.getTransaction() != null)
               {
                  // Disassociate ctx with tx
                  deregister(entityCtx, current);
               }
            }
         }
      } else
      {
         // No tx
         
         // Synchronize with DB
//DEBUG         Logger.debug("SYNCH");
         if (!entityCtx.isSynchronized())
         {
            ((EntityContainer)getContainer()).getPersistenceManager().loadEntity(entityCtx);
            entityCtx.setSynchronized(true);
         }
         
         try
         {
            Object result = getNext().invoke(id, method, args, ctx);
         
            // Store after each invocation -- not on exception though, or removal   
            if (ctx.getId() != null && !method.getName().startsWith("get"))
               ((EntityContainer)getContainer()).getPersistenceManager().storeEntity((EntityEnterpriseContext)ctx);
            
            return result;
         } catch (Exception e)
         {
            // Exception - force reload on next call
            entityCtx.setSynchronized(false);
            throw e;
         }
      }
   }
   
   // Protected  ----------------------------------------------------

   // Inner classes -------------------------------------------------
   class InstanceSynchronization
      implements Synchronization
   {
      ArrayList ctxList = new ArrayList();
      Transaction tx;
      
      InstanceSynchronization(Transaction tx)
      {
         this.tx = tx;
      }
      
      public void add(EnterpriseContext ctx)
      {
         ctxList.add(ctx.getId());
      }
      
      public void remove(EnterpriseContext ctx)
      {
         ctxList.remove(ctx.getId());
      }
      
      // Synchronization implementation -----------------------------
      public void beforeCompletion()
      {
         InstanceCache cache = ((EntityContainer)getContainer()).getInstanceCache();
         
         try
         {
            
            for (int i = 0; i < ctxList.size(); i++)
            {
               // Lock instance
               try
               {
                  EntityEnterpriseContext ctx = (EntityEnterpriseContext)cache.get(ctxList.get(i));
                  // Store instance if business method was
                  if (ctx.isInvoked())
                     ((EntityContainer)getContainer()).getPersistenceManager().storeEntity(ctx);
                  
                  // Save for after completion
                  ctxList.set(i, ctx);
               } catch (NoSuchEntityException e)
               {
                  // Object has been removed -- ignore
               }
            }
         } catch (RemoteException e)
         {
            // Store failed -> rollback!
            try
            {
               tx.setRollbackOnly();
            } catch (SystemException ex)
            {
               ex.printStackTrace();
            }
         }
      }
      
      public void afterCompletion(int status)
      {
         InstanceCache cache = ((EntityContainer)getContainer()).getInstanceCache();
         InstancePool pool = getContainer().getInstancePool();
         
         // If rolled back -> invalidate instance
         if (status == Status.STATUS_ROLLEDBACK)
         {

            for (int i = 0; i < ctxList.size(); i++)
            {
               try
               {
                  EntityEnterpriseContext ctx = (EntityEnterpriseContext)ctxList.get(i);
                  
                  ctx.setId(null);
                  ctx.setTransaction(null);
                  cache.remove(ctx);
                  pool.free(ctx); // TODO: should this be done? still valid instance?
               } catch (Exception e)
               {
                  // Ignore
               }
            }
         } else
         {
            for (int i = 0; i < ctxList.size(); i++)
            {
					switch (commitOption)
					{
						// Keep instance cached after tx commit
						case A:
							try
							{
							   EntityEnterpriseContext ctx = (EntityEnterpriseContext)ctxList.get(i);
							   ctx.setTransaction(null);
							   ctx.setInvoked(false);
							   cache.release(ctx);
							} catch (Exception e)
							{
							   Logger.debug(e);
							}
							break;
						
						// Keep instance, but invalidate state
						case B:
							try
							{
							   EntityEnterpriseContext ctx = (EntityEnterpriseContext)ctxList.get(i);
							   ctx.setTransaction(null);
							   ctx.setInvoked(false);
								
								ctx.setSynchronized(false); // Invalidate state
							   cache.release(ctx);
							} catch (Exception e)
							{
							   Logger.debug(e);
							}
							break;
							
					// Passivate instance
					case C:
						try
						{
						   EntityEnterpriseContext ctx = (EntityEnterpriseContext)ctxList.get(i);
						   ctx.setTransaction(null);
						   ctx.setInvoked(false);
							
							((EntityContainer)getContainer()).getPersistenceManager().passivateEntity(ctx); // Passivate instance
						   cache.remove(ctx.getId()); // Remove from cache
							
							pool.free(ctx); // Add to instance pool
							
						} catch (Exception e)
						{
						   Logger.debug(e);
						}
						break;
					}
				
            }
         }
         
         // Remove from tx/synch mapping
         synchs.remove(this);
         
         // Notify all who are waiting for this tx to end
         synchronized (tx)
         {
            tx.notifyAll();
         }
      }
   }
}

