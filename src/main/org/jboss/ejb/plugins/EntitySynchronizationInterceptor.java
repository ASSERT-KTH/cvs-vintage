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

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.MethodInvocation;

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
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.16 $
*/
public class EntitySynchronizationInterceptor
extends AbstractInterceptor
{
	// Constants -----------------------------------------------------
	
	/**
	*  Cache a "ready" instance between transactions.
	*  Data will <em>not</em> be reloaded from persistent storage when
	*  a new transaction is started.
	*  This option should only be used if the instance has exclusive
	*  access to its persistent storage.
	*/
	public static final int A = 0; // Keep instance cached
	
	/**
	*  Cache a "ready" instance between transactions and reload data
	*  from persistent storage on transaction start.
	*/
	public static final int B = 1; // Invalidate state
	
	/**
	*  Passivate instance after each transaction.
	*/
	public static final int C = 2; // Passivate
	
	// Attributes ----------------------------------------------------
	
	/**
	*  The current commit option.
	*/
	protected int commitOption = A;
	
	/**
	*  The container of this interceptor.
	*/
	protected EntityContainer container;
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	public void setContainer(Container container) 
	{ 
		this.container = (EntityContainer)container; 
	}
	
	public Container getContainer()
	{
		return container;
	}
	
	/**
	*  Setter for property commitOption.
	*/
	public void setCommitOption(int commitOption)
	{
		this.commitOption = commitOption;
	}
	
	/**
	*  Getter for property commitOption.
	*/
	public int getCommitOption()
	{
		return commitOption;
	}
	
	/**
	*  Register a transaction synchronization callback with a context.
	*/
	private void register(EntityEnterpriseContext ctx, Transaction tx)
	{
		// Create a new synchronization
		InstanceSynchronization synch = new InstanceSynchronization(tx, ctx);
		
		try {
			// OSH: An extra check to avoid warning.
			// Can go when we are sure that we no longer get
			// the JTA violation warning.
			if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
				
				ctx.setValid(false);
				
				return;
			}
			
			// We want to be notified when the transaction commits
			tx.registerSynchronization(synch);
		
		} catch (RollbackException e) {
			
			// The state in the instance is to be discarded, we force a reload of state
			ctx.setValid(false);
		
		} catch (Exception e) {
			
			throw new EJBException(e);
		
		}
	}
	
	private void deregister(EntityEnterpriseContext ctx)
	{
		// MF FIXME: I suspect this is redundant now
		// (won't the pool clean it up?)
		
		// Deassociate ctx with tx
		// OSH: TxInterceptor seems to do this: ctx.setTransaction(null);
		// OSH: Pool seems to do this: ctx.setInvoked(false);
	}
	
	// Interceptor implementation --------------------------------------
	
	public Object invokeHome(MethodInvocation mi)
	throws Exception
	{
		try {
			return getNext().invokeHome(mi);
		} finally {
			// Anonymous was sent in, so if it has an id it was created
			EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
			if (ctx.getId() != null) {
				Transaction tx = mi.getTransaction();
				
				if (tx != null && tx.getStatus() == Status.STATUS_ACTIVE)
					register(ctx, tx); // Set tx
				
				// Currently synched with underlying storage
				ctx.setValid(true);
			}
		}
	}
	
	public Object invoke(MethodInvocation mi)
	throws Exception
	{
		// We are going to work with the context a lot
		EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
		
		// The Tx coming as part of the Method Invocation 
		Transaction tx = mi.getTransaction();
		
		//Logger.debug("CTX in: isValid():"+ctx.isValid()+" isInvoked():"+ctx.isInvoked());
		//Logger.debug("newTx: "+ tx);
		
		// Is my state valid?
		if (!ctx.isValid()) {
			
			// If not tell the persistence manager to load the state
			((EntityContainer)getContainer()).getPersistenceManager().loadEntity(ctx);
			
			// Now the state is valid
			ctx.setValid(true);
		}
		
		// So we can go on with the invocation
		Logger.log("Tx is "+ ((tx == null)? "null" : tx.toString()));
		
		// Invocation with a running Transaction
		
		if (tx != null && tx.getStatus() == Status.STATUS_ACTIVE) {
			
			try {
				
				//Invoke down the chain
				return getNext().invoke(mi);
			
			} 
			
			finally {
				
				// Do we have a valid bean (not removed)
				if (ctx.getId() != null) {
					
					// If the context was not invoked previously...
					if (!ctx.isInvoked()) {
						
						// It is now and this will cause ejbStore to be called...
						ctx.setInvoked(true);
						
						// ... on a transaction callback that we register here.
						register(ctx, tx);
					}
				}
				
				// Entity was removed
				else {
					
					if (ctx.getTransaction() != null) {
						
						//DEBUG Logger.debug("CTX out: isValid():"+ctx.isValid()+" isInvoked():"+ctx.isInvoked());
						//DEBUG Logger.debug("PresentTx:"+tx);
						
						// If a ctx still has a transaction we would need to deresgister the sync
						// The simplest is to tell the pool to kill the instance if tx is present
					
					}
				}
			}
		}
		
		//    
		else { // No tx
			try {
				Object result = getNext().invoke(mi);
				
				// Store after each invocation -- not on exception though, or removal
				// And skip reads too ("get" methods)
				// OSH FIXME: Isn't this startsWith("get") optimization a violation of
				// the EJB specification? Think of SequenceBean.getNext().
				if (ctx.getId() != null && !mi.getMethod().getName().startsWith("get"))
					((EntityContainer)getContainer()).getPersistenceManager().storeEntity(ctx);
				
				return result;
			} catch (Exception e) {
				// Exception - force reload on next call
				ctx.setValid(false);
				throw e;
			}
		}
	}
	
	
	// Protected  ----------------------------------------------------
	
	// Inner classes -------------------------------------------------
	
	private class InstanceSynchronization
	implements Synchronization
	{
		/**
		*  The transaction we follow.
		*/
		private Transaction tx;
		
		/**
		*  The context we manage.
		*/
		private EntityEnterpriseContext ctx;
		
		/**
		*  Create a new instance synchronization instance.
		*/
		InstanceSynchronization(Transaction tx, EntityEnterpriseContext ctx)
		{
			this.tx = tx;
			this.ctx = ctx;
		}
		
		// Synchronization implementation -----------------------------
		
		public void beforeCompletion()
		{
			Logger.log("beforeCompletion called for ctx "+ctx.hashCode());
			
			if (ctx.getId() != null) {
				try {
					try {
						
						// MF FIXME: should we throw an exception if lock is present (app error)
						// it would mean that someone is commiting when all the work is not done
						
						// Store instance if business method was invoked
						if (ctx.isInvoked())  {
							
							//DEBUG Logger.log("EntitySynchronization sync calling store on ctx "+ctx.hashCode());
							
							Logger.log("EntitySynchronization sync calling store on ctx "+ctx.hashCode());
							
							container.getPersistenceManager().storeEntity(ctx);
						}
					} catch (NoSuchEntityException e) {
						// Object has been removed -- ignore
					}
				} catch (RemoteException e) {
					Logger.exception(e);
					
					// Store failed -> rollback!
					try {
						tx.setRollbackOnly();
					} catch (SystemException ex) {
						// DEBUG ex.printStackTrace();
					}
				}
			}
		}
		
		public void afterCompletion(int status)
		{
			if (ctx.getId() != null) {
				Logger.log("afterCompletion called for ctx "+ctx.hashCode());
				// If rolled back -> invalidate instance
				if (status == Status.STATUS_ROLLEDBACK) {
					try {
						
						// finish the transaction association
						ctx.setTransaction(null);
						
						ctx.setValid(false); 
							
						// remove from the cache
						container.getInstanceCache().remove(ctx.getId());
						
						// return to pool
						container.getInstancePool().free(ctx); 
						
						// wake threads waiting
						synchronized(ctx) { ctx.notifyAll();}
					
					} catch (Exception e) {
						// Ignore
					}
				
				
				
				} else {
					// The transaction is done
					ctx.setTransaction(null);
					
					// We are afterCompletion so the invoked can be set to false (db sync is done)
					ctx.setInvoked(false);
					
					switch (commitOption) {
						// Keep instance cached after tx commit
						case A:
							try {
								// The state is still valid (only point of access is us)
								ctx.setValid(true); 
						
							} catch (Exception e) {
								Logger.debug(e);
							}
						break;
						
						// Keep instance active, but invalidate state
						case B:
							try {
								// Invalidate state (there might be other points of entry)
								ctx.setValid(false); 
								
							} catch (Exception e) {
								Logger.debug(e);
							}
						break;
						
						// Invalidate everything AND Passivate instance
						case C:
							try {
								
								// Passivate instance
								container.getPersistenceManager().passivateEntity(ctx); 
								
								// Remove from the cache, it is not active anymore
								container.getInstanceCache().remove(ctx.getId()); 
								
								// Back to the pool
								container.getInstancePool().free(ctx); 
							} catch (Exception e) {
								Logger.debug(e);
							}
						break;
					}
				}
			}
			// Notify all who are waiting for this tx to end, they are waiting since the locking logic
			synchronized (ctx) {ctx.notifyAll();}
		}
	}
}

