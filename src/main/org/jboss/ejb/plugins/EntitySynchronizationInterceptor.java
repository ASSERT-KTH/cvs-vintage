/**
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;

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
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.logging.Logger;
import org.jboss.util.Sync;

/**
 * The role of this interceptor is to synchronize the state of the cache with
 * the underlying storage.  It does this with the ejbLoad and ejbStore
 * semantics of the EJB specification.  In the presence of a transaction this
 * is triggered by transaction demarcation. It registers a callback with the
 * underlying transaction monitor through the JTA interfaces.  If there is no
 * transaction the policy is to store state upon returning from invocation.
 * The synchronization polices A,B,C of the specification are taken care of
 * here.  
 *
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 *    before changing.
 *    
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.40 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/06/28: marcf</b>
 * <ol>
 *   <li>Moved to new synchronization
 *   <li>afterCompletion doesn't return to pool anymore, idea is to simplify 
 *       design by not mucking with reuse of the instances
 *   <li>before completion checks for a rolledback tx and doesn't call the 
 *       store in case of a rollback we are notified but we don't register
 *       the resource
 * </ol>
 */
public class EntitySynchronizationInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------
	
   /**
    *  The current commit option.
    */
   protected int commitOption;
	
   /**
    *  The refresh rate for commit option d
    */
   protected long optionDRefreshRate;
	
   /**
    *  The container of this interceptor.
    */
   protected EntityContainer container;
	
   /**
    *  Optional isModified method
    */
   protected Method isModified;
	
   /**
    *  For commit option D this is the cache of valid entities
    */
   protected HashSet validContexts;
	
   // Static --------------------------------------------------------
	
   // Constructors --------------------------------------------------
	
   // Public --------------------------------------------------------
   
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
   }
	
   public void init()
      throws Exception
   {
		
      try{
			
         validContexts = new HashSet();
         ConfigurationMetaData configuration = container.getBeanMetaData().getContainerConfiguration();
         commitOption = configuration.getCommitOption();
         optionDRefreshRate = configuration.getOptionDRefreshRate();
			
         //start up the validContexts thread if commit option D
         if(commitOption == ConfigurationMetaData.D_COMMIT_OPTION){
            ValidContextsRefresher vcr = new ValidContextsRefresher(validContexts, optionDRefreshRate);
            new Thread(vcr).start();
         }
			
         isModified = container.getBeanClass().getMethod("isModified", new Class[0]);
         if (!isModified.getReturnType().equals(Boolean.TYPE))
            isModified = null; // Has to have "boolean" as return type!
      } catch (Exception e)
      {
         System.out.println(e.getMessage());
      }
   }
	
   public Container getContainer()
   {
      return container;
   }
	
   /**
    *  Register a transaction synchronization callback with a context.
    */
   private void register(EntityEnterpriseContext ctx, Transaction tx)
   {
      // Create a new synchronization
      InstanceSynchronization synch = new InstanceSynchronization(tx, ctx);
		
      try {
         // associate the entity bean with the transaction so that
         // we can do things like synchronizeEntitiesWithinTransaction
         ((EntityContainer)ctx.getContainer()).getTxEntityMap().associate(tx, ctx);  
			
         // We want to be notified when the transaction commits
         tx.registerSynchronization(synch);
			
         // marcf: rethink the need for synchronization here, who else uses it?
         synchronized(ctx.getTxLock()) {ctx.setTxSynchronized(true);}
					
      } catch (RollbackException e) {
			
         //Indicates that the transaction is already marked for rollback 
         ((EntityContainer)ctx.getContainer()).getTxEntityMap().disassociate(tx, ctx);  
			
         // The state in the instance is to be discarded, we force a reload of state	
         synchronized (ctx) {ctx.setValid(false);}
			
         // This is really a mistake from the JTA spec, the fact that the tx is marked rollback should not be relevant
         // We should still hear about the demarcation
         synchronized (ctx.getTxLock()) 
         {	
            // We couldn't register the synchronization 			
            ctx.setTransaction(null); ctx.setTxSynchronized(false);
				
            // Wake up threads waiting for the transaction demarcation, ain't gonna happen
            ctx.getTxLock().notifyAll();
         }
		
      } catch (Exception e) {
			
         ((EntityContainer)ctx.getContainer()).getTxEntityMap().disassociate(tx, ctx);  
			
         // If anything goes wrong with the association remove the ctx-tx association
         synchronized (ctx.getTxLock()) { ctx.setTransaction(null); ctx.setTxSynchronized(false);}
			
         throw new EJBException(e);
		
      }
   }
	
	
   /**
    * As per the spec 9.6.4, entities must be synchronized with the datastore
    * when an ejbFind<METHOD> is called.
    */
   private void synchronizeEntitiesWithinTransaction(Transaction tx) throws Exception
   {
      Object[] entities = ((EntityContainer)getContainer()).getTxEntityMap().getEntities(tx);
      for (int i = 0; i < entities.length; i++)
      {
         EntityEnterpriseContext ctx = (EntityEnterpriseContext)entities[i];
         storeEntity(ctx);
      }
   }
	
   private void storeEntity(EntityEnterpriseContext ctx) throws Exception
   {
      if (ctx.getId() != null)
      {
         boolean dirty = true;
         // Check isModified bean method flag
         if (isModified != null)
         {
            dirty = ((Boolean)isModified.invoke(ctx.getInstance(), new Object[0])).booleanValue();
         }
			
         // Store entity
         if (dirty)
            ((EntityContainer)getContainer()).getPersistenceManager().storeEntity(ctx);
      }
   }
   
   // Interceptor implementation --------------------------------------
	
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
		
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)mi.getEnterpriseContext();
      Transaction tx = mi.getTransaction();
		
      try 
      {
         if (tx != null && mi.getMethod().getName().startsWith("find"))
         {
            // As per the spec EJB2.0 9.6.4, entities must be synchronized with the datastore
            // when an ejbFind<METHOD> is called.
            synchronizeEntitiesWithinTransaction(tx);
         }
			
         return getNext().invokeHome(mi);
		
      } finally {
			
         // An anonymous context was sent in, so if it has an id it is a real instance now
         if (ctx.getId() != null) {
				
            // Currently synched with underlying storage
            ctx.setValid(true);
				
            if (tx!= null) register(ctx, tx); // Set tx
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
		
      //Commit Option D....
      if(commitOption == ConfigurationMetaData.D_COMMIT_OPTION && !validContexts.contains(ctx.getId())){
         //bean isn't in cache
         //so set valid to false so that we load...
         ctx.setValid(false);
      }
				
      // Is my state valid?
      if (!ctx.isValid()) {
			
         // If not tell the persistence manager to load the state
         ((EntityContainer)getContainer()).getPersistenceManager().loadEntity(ctx);
			
         // Now the state is valid
         ctx.setValid(true);
      }
		
      // So we can go on with the invocation
      //DEBUG		Logger.debug("Tx is "+ ((tx == null)? "null" : tx.toString()));
		
      // Invocation with a running Transaction
      if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
			
         try {
				
            //Invoke down the chain
            return getNext().invoke(mi);
			
         }
			
         finally {
				
            //register the wrapper with the transaction monitor (but only register once).
            // The transaction demarcation will trigger the storage operations
            if (!ctx.isTxSynchronized()) register(ctx,tx);
         }
      }
		
      //
      else { // No tx
         try {
				
            Object result = getNext().invoke(mi);
				
            // Store after each invocation -- not on exception though, or removal
            // And skip reads too ("get" methods)
            if (ctx.getId() != null)
            {
               storeEntity(ctx);
            }
				
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
         // DEBUG Logger.debug("beforeCompletion called for ctx "+ctx.hashCode());
			
         if (ctx.getId() != null) {
				
            // This is an independent point of entry. We need to make sure the
            // thread is associated with the right context class loader
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(container.getClassLoader());
				
            try 
            {
               try 
               {
						
                  // Store instance if business method was invoked
                  if (ctx.getTransaction().getStatus() != Status.STATUS_MARKED_ROLLBACK)
                  {
							
                     //DEBUG Logger.debug("EntitySynchronization sync calling store on ctx "+ctx.hashCode());
							
                     // Check if the bean defines the isModified method
                     boolean dirty = true;
                     if (isModified != null)
                     {
                        try
                        {
                           dirty = ((Boolean)isModified.invoke(ctx.getInstance(), new Object[0])).booleanValue();
                        } catch (Exception ignored) {}
                     }
							
                     if (dirty)
                        container.getPersistenceManager().storeEntity(ctx);
                  }
               } catch (NoSuchEntityException ignored) {
                  // Object has been removed -- ignore
               }
            }
            // EJB 1.1 12.3.2: all exceptions from ejbStore must be marked for rollback
            // and the instance must be discarded
            catch (Exception e) {
               Logger.exception(e);
					
               // Store failed -> rollback!
               try {
                  tx.setRollbackOnly();
               } catch (SystemException ex) {
                  // DEBUG ex.printStackTrace();
               } catch (IllegalStateException ex) {
                  // DEBUG ex.printStackTrace();
               }
            }
            finally {
					
               Thread.currentThread().setContextClassLoader(oldCl);
            }
         }
      }
		
      public void afterCompletion(int status)
      {
			
         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(container.getClassLoader());
			
         synchronized (ctx) 
         {
            try
            {			
               // If rolled back -> invalidate instance
               if (status == Status.STATUS_ROLLEDBACK) {
						
                  // remove from the cache
                  container.getInstanceCache().remove(ctx.getCacheKey());
						
                  // Wake all those waiting that the context is no longer in cache
                  ctx.notifyAll();
					
               } else {
						
                  switch (commitOption) {
                     // Keep instance cached after tx commit
                   case ConfigurationMetaData.A_COMMIT_OPTION:
                      // The state is still valid (only point of access is us)
                      ctx.setValid(true);
                      break;
							
                      // Keep instance active, but invalidate state
                   case ConfigurationMetaData.B_COMMIT_OPTION:
                      // Invalidate state (there might be other points of entry)
                      ctx.setValid(false);
                      break;
                      // Invalidate everything AND Passivate instance
                   case ConfigurationMetaData.C_COMMIT_OPTION:
                      try {
                         container.getInstanceCache().release(ctx);
                      } catch (Exception e) {
                         Logger.debug(e);
                      }
                      break;
                   case ConfigurationMetaData.D_COMMIT_OPTION:
                      //if the local cache is emptied then valid is set to false(see invoke() )
                      validContexts.add(ctx.getId());
                      break;
                  }
               }	
            } 
				
            finally {
					
               synchronized(ctx) {
						
                  // finish the transaction association
                  ((EntityContainer)ctx.getContainer()).getTxEntityMap().disassociate(tx, ctx);  
               }
					
               // All the threads waiting for the end of the transaction need to wake up and 
               // go lock on the next thing, either ctx for those threads that will win the 
               // transactional race or ctx.txLock for the rest of them (better luck next time)
               // no-one must stay on this txLock since the tx is going away so this is the one 
               // and only call!.
               // So don't replace the following by "notify()" (!)
               synchronized(ctx.getTxLock()) 
               {
                  // The context is no longer synchronized on the TX
                  ctx.setTxSynchronized(false); ctx.setTransaction(null);
						
                  ctx.getTxLock().notifyAll();
               }
					
               Thread.currentThread().setContextClassLoader(oldCl);
				
            }
         }
      }
   }
	
   class ValidContextsRefresher implements Runnable {
      private HashSet validContexts;
      private long refreshRate;
		
      public ValidContextsRefresher(HashSet validContexts,long refreshRate){
         this.validContexts = validContexts;
         this.refreshRate = refreshRate;
      }
		
      public void run(){
         while(true){
            validContexts.clear();
            // debug System.out.println("Flushing the valid contexts");
            try{
               Thread.sleep(refreshRate);
            }catch(Exception e){
               System.out.println(e.getMessage());
            }
         }
      }
   }
}

