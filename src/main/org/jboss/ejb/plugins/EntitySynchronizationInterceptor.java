/*
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
*   @version $Revision: 1.34 $
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
         // OSH: An extra check to avoid warning.
         // Can go when we are sure that we no longer get
         // the JTA violation warning.

		 // SA FIXME. this is a bad check. when minerva marks rollback, we still should
		 // be notified of the tx demarcation.

		 //if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {

         // ctx.setValid(false);

         // return;
         //}

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

       //Commit Option D....
       if(commitOption == ConfigurationMetaData.D_COMMIT_OPTION && !validContexts.contains(ctx.getId())){
		   //bean isn't in cache
		   //so set valid to false so that we load...
		   ctx.setValid(false);
	   }

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
       //DEBUG		Logger.debug("Tx is "+ ((tx == null)? "null" : tx.toString()));

       // Invocation with a running Transaction

       if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {

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
          if (ctx.getId() != null)
          {
              boolean dirty = true;
              // Check isModified bean flag
              if (isModified != null)
              {
                 dirty = ((Boolean)isModified.invoke(ctx.getInstance(), new Object[0])).booleanValue();
              }

              // Store entity
              if (dirty)
                 ((EntityContainer)getContainer()).getPersistenceManager().storeEntity(ctx);
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

          try {

              try {

                 // MF FIXME: should we throw an exception if lock is present (app error)
                 // it would mean that someone is commiting when all the work is not done

                 // Store instance if business method was invoked
                 if (ctx.isInvoked()) {

                   //DEBUG Logger.debug("EntitySynchronization sync calling store on ctx "+ctx.hashCode());

                   // Check isModified bean flag
                   boolean dirty = true;
                   if (isModified != null)
                   {
                    try
                    {
                        dirty = ((Boolean)isModified.invoke(ctx.getInstance(), new Object[0])).booleanValue();
                    } catch (Exception e)
                    {
                        // Ignore
                    }
                   }

                   if (dirty)
                    container.getPersistenceManager().storeEntity(ctx);
                 }
              } catch (NoSuchEntityException e) {
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

         try
         {

          //DEBUG Logger.debug("afterCompletion called for ctx "+ctx.hashCode());

          // If rolled back -> invalidate instance
          // If removed -> send back to the pool
          if (status == Status.STATUS_ROLLEDBACK || ctx.getId() == null) {

              try {

                 // finish the transaction association
                 ctx.setTransaction(null);

                 // remove from the cache
                 container.getInstanceCache().remove(ctx.getCacheKey());

                 // return to pool
                 container.getInstancePool().free(ctx);

              } catch (Exception e) {
                 // Ignore
              }

          } else {

              // We are afterCompletion so the invoked can be set to false (db sync is done)
              ctx.setInvoked(false);

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
                 	//add to cache....
                 	//if the cache doesn't time out valid remains true
                 	//if the cache is emptied then valid is set to false(see invoke() )
					validContexts.add(ctx.getId());
                 break;
              }

              // finish the transaction association
              ctx.setTransaction(null);
          }
         }

         finally {

          Thread.currentThread().setContextClassLoader(oldCl);

         }
       }
    }

class ValidContextsRefresher implements Runnable{
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

