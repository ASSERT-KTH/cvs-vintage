/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.logging.Logger;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.tm.TransactionLocal;

/**
 * This class provides a way to find out what entities are contained in
 * what transaction.  It is used, to find which entities to call ejbStore()
 * on when a ejbFind() method is called within a transaction. EJB 2.0- 9.6.4
 * also, it is used to synchronize on a remove.
 * Used in EntitySynchronizationInterceptor, EntityContainer
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public final class EntityInvocationRegistry
{
   /**
    * The transaction manager is maintained by the system and
    * manges the assocation of transaction to threads.
    */
   private final TransactionManager transactionManager;

   /**
    * Holds the entities associated with the each transaction.
    */
   private final TransactionLocal entityMapTxLocal;

   /**
    * The log.
    */
   private final Logger log = Logger.getLogger(getClass());

   /**
    * Creates a new EntityInvocationRegistry.  Normally there is one registry 
    * for the entire JBoss server, but eventually we should move this to a 
    * domain level object.
    * @throws IllegalStateException if transaction manager is not regstered at
    * java:/TransactionManager
    */
   public EntityInvocationRegistry()
   {
      // Get the transaction manager
      try
      {
         InitialContext context = new InitialContext();
         transactionManager = (TransactionManager) context.lookup(
               "java:/TransactionManager");
      }
      catch(NamingException e)
      {
         throw new IllegalStateException("An error occured while " +
               "looking up the transaction manager: " + e);
      }

      // Create the transaction local to hold the entity sets
      entityMapTxLocal = new TransactionLocal(new EntitySynchronization())
      {
         protected Object initialValue() 
         {
            return new HashMap();
         }
      };
   }

   /**
    * Associate entity with registry.
    */
   public void associate(EntityEnterpriseContext ctx)
   {
      associate(ctx, getTransaction());
   }
   
   /**
    * Associate entity with registry.
    */
   public void associate(EntityEnterpriseContext ctx, Transaction tx)
   {
      EntityContextKey key = new EntityContextKey(
            (EntityContainer)ctx.getContainer(),
            ctx.getId());

      // if this is an previously unseen context...
      Map entityMap = (Map) entityMapTxLocal.get(tx);
      if(entityMap.put(key, ctx) == null)
      {
         if(log.isTraceEnabled())
         {
            log.trace("Associated new entity: " +
                  "ejb=" + key.getContainer().getBeanMetaData().getEjbName() + 
                  ", id=" + key.getId());
         }
      }
   }

   /**
    * Gets the EntityEnterpriseContext for the current transaction in the 
    * specified container with the specified key.
    */
   public synchronized EntityEnterpriseContext getContext(
         EntityContainer container,
         Object id)
   {
      return getContext(container, id, getTransaction());
   }
            
   /**
    * Gets the EntityEnterpriseContext for the current transaction in the 
    * specified container with the specified key.
    */
   public EntityEnterpriseContext getContext(
         EntityContainer container,
         Object id,
         Transaction tx)
   {
      EntityContextKey key = new EntityContextKey(container, id);

      Map entityMap = (Map) entityMapTxLocal.get(tx);
      return (EntityEnterpriseContext)entityMap.get(key);
    }

   /**
    * Sync all EntityEnterpriseContext that are involved (and changed)
    * within a transaction.
    */
   public void synchronizeEntities() 
   {
      synchronizeEntities(getTransaction());
   }

   /**
    * Sync all EntityEnterpriseContext that are involved (and changed)
    * within a transaction.
    */
   public void synchronizeEntities(Transaction tx) 
   {
      // There should be only one thread associated with this tx at a time.
      // Therefore we should not need to synchronize on entityMap to ensure
      // exclusive access.  
      Map entityMap = (Map)entityMapTxLocal.get(tx);

      // This is an independent point of entry. We need to make sure the
      // thread is associated with the right context class loader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      EntityEnterpriseContext ctx = null;
      EntityContainer container = null;
      try
      {
         for(Iterator iter = entityMap.values().iterator(); iter.hasNext(); )
         {
            // any one can mark the tx rollback at any time so check
            // before continuing to the next store
            if(transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK) 
            {
               // nothing else to do here
               return;
            } 

            // read-only entities will never get into this list.
            ctx = (EntityEnterpriseContext)iter.next();
            container = (EntityContainer)ctx.getContainer();

            // only synchronize if the id is not null.  A null id means 
            // that the entity has been removed.
            if(ctx.getId() != null)
            {

               // set the context class loader before calling the store method
               Thread.currentThread().setContextClassLoader(
                     container.getClassLoader());

               // store it
               if(log.isTraceEnabled())
               {
                  log.trace("Synchronizing entity: " + 
                        "ejb=" + container.getBeanMetaData().getEjbName() + 
                        ", id=" + ctx.getId());
               }
               container.storeEntity(ctx);
            }
         }
      }
      catch (Exception e)
      {
         // EJB 1.1 section 12.3.2 and EJB 2 section 18.3.3
         // exception during store must log exception, mark tx for 
         // rollback and throw a TransactionRolledback[Local]Exception
         // if using caller's transaction.  All of this is handled by 
         // the AbstractTxInterceptor and LogInterceptor.
         //
         // however we may need to ignore a NoSuchEntityException -- TODO
         //
         if(e instanceof EJBException)
         {
            throw (EJBException)e;
         }
         throw new EJBException("Exception in store of entity: " +
               "ejb=" + container.getBeanMetaData().getEjbName() + 
               ", id=" + ctx.getId(), e);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * Disassociate entity with transaction.
    */
   private void disassociateEntity(Object id, EntityEnterpriseContext ctx) 
   {
      // Get the container associated with this context
      EntityContainer container = (EntityContainer)ctx.getContainer();

      boolean trace = log.isTraceEnabled();
      if(trace)
      {
         log.trace("Disassociate entity: " +
               "ejb=" + container.getBeanMetaData().getEjbName() + 
               ", id=" + id);
      }

      // This is an independent point of entry. We need to make sure the
      // thread is associated with the right context class loader
      // set the context class loader before calling the store method
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
      try
      {
         Transaction transaction = transactionManager.getTransaction();
         BeanLock lock = container.getLockManager().getLock(id);
         lock.sync();
         try
         {
            ConfigurationMetaData configuration = 
               container.getBeanMetaData().getContainerConfiguration();
            int commitOption = configuration.getCommitOption();
            int status = transaction.getStatus();

            if(container.isMultiInstance())
            {
               if(commitOption == ConfigurationMetaData.A_COMMIT_OPTION ||
                  commitOption == ConfigurationMetaData.D_COMMIT_OPTION)
               {
                  throw new IllegalStateException("Commit option A or D not allowed with this Interceptor: " +
                     "ejb=" + container.getBeanMetaData().getEjbName());
               }

               // multi instance contexts are never put reused
               // pasivate and free the context
               try
               {
                  container.passivateEntity(ctx);
               }
               catch (Exception ignored) 
               {
                  log.warn("Error in passivation of entity: " +
                     "ejb=" + container.getBeanMetaData().getEjbName() + 
                     ", id=" + id +
                     ", ctx=" + ctx + 
                     ", transaction=" + transaction);
               }
               container.getInstancePool().free(ctx);
            }
            else
            {
               // If rolled back, invalidate instance
               if(status == Status.STATUS_ROLLEDBACK)
               {
                  // remove from the cache
                  ctx.getContainer().getInstanceCache().remove(id);
               }
               else if(commitOption == ConfigurationMetaData.A_COMMIT_OPTION)
               {
                  // Keep instance cached after tx commit
                  // The state is still valid (only point of access is us)
                  ctx.setValid(true);
               }
               else if(commitOption == ConfigurationMetaData.B_COMMIT_OPTION)
               {
                  // Keep instance active, but invalidate state
                  // Invalidate state (there might be other points of entry)
                  ctx.setValid(false);
               }
               else if(commitOption == ConfigurationMetaData.C_COMMIT_OPTION)
               {
                  // Invalidate everything AND Passivate instance
                  try
                  {
                     // Do not call release if getId() is null.  This means 
                     // that the entity has been removed from cache.
                     // Release will schedule a passivation and this removed
                     // ctx could be put back into the cache!
                     if(ctx.getId() != null)
                     {
                        ctx.getContainer().getInstanceCache().release(ctx);
                     }
                  }
                  catch (Exception e)
                  {
                     log.debug("Exception releasing context", e);
                  }
               }
               else if(commitOption == ConfigurationMetaData.D_COMMIT_OPTION)
               {
                  // TODO:  Commot option D should be replace with a timed 
                  // cache invalidator
                  ctx.setValid(true);
               }
            }
         }
         finally
         {
            if(trace)
            {
               log.trace("Clearing transaction lock: " +
                     "ejb=" + container.getBeanMetaData().getEjbName() + 
                     ", id=" + id +
                     ", ctx=" + ctx + 
                     ", transaction=" + transaction);
            }

            // The context is no longer synchronized on the TX
            ctx.hasTxSynchronization(false);
            ctx.setTransaction(null);
            try
            {
               lock.endTransaction(transaction);

               if(trace)
               {
                  log.trace("Sending notify on TxLock: " +
                        "ejb=" + container.getBeanMetaData().getEjbName() + 
                        ", id=" + id +
                        ", ctx=" + ctx + 
                        ", transaction=" + transaction);
               }
            } 
            finally 
            {
               lock.releaseSync();
            }
         }
      }
      catch(SystemException e)
      {
         log.error("Failed to get thread context transaction: ", e);
      }
      catch(InterruptedException e)
      {
         log.error("Failed to acquire lock.sync: ", e);
      }
      finally
      {
         // reset the classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   /**
    * Gets the transaction associated with the current thread.
    * @throws EJBException if an error occurs while getting the transaction
    */
   private Transaction getTransaction() 
   {
      try
      {
         return transactionManager.getTransaction();
      }
      catch(SystemException e)
      {
         throw new EJBException("An error occured while " +
               "looking getting the current transaction" + e);
      }
   }


   private final class EntityContextKey
   {
      private final EntityContainer container;
      private final Object id;
      
      public EntityContextKey(final EntityContainer container, final Object id)
      {
         if(container == null)
         {
            throw new IllegalArgumentException("Container is null");
         }
         if(id == null)
         {
            throw new IllegalArgumentException("Id is null");
         }
         this.container = container;
         this.id = id;
      }

      public EntityContainer getContainer() 
      {
         return container;
      }

      public Object getId()
      {
         return id;
      }

      public boolean equals(Object object)
      {
         if(!(object instanceof EntityContextKey))
         {
            return false;
         }

         EntityContextKey key = (EntityContextKey)object;
         return container.equals(key.getContainer()) && id.equals(key.getId());
      }

      public int hashCode()
      {
         int result = 17;
         result = 37*result + container.hashCode();
         result = 37*result + id.hashCode();
         return result;
      }
   }
   
   /**
    * EntitySynchronization synchronizes the entity state before the 
    * transaction completes, and handles the commit options after the
    * transaction completes.
    */
   private final class EntitySynchronization implements Synchronization
   {
      public void beforeCompletion()
      {
         if(log.isTraceEnabled()) 
         {
            log.trace("beforeCompletion called");
         }

         // let the runtime exceptions fall out, so the committer can determine
         // the root cause of a rollback
         synchronizeEntities();
      }

      public void afterCompletion(int status)
      {

         // There should be only one thread associated with this tx at a time.
         // Therefore we should not need to synchronize on entityList to ensure
         // exclusive access.  EntityList is correct since it was obtained in a 
         // synch block.
         Map entityMap = (Map)entityMapTxLocal.get();
         for(Iterator iter = entityMap.entrySet().iterator(); iter.hasNext();)
         {
            Map.Entry entry = (Map.Entry)iter.next();
            disassociateEntity(
                  ((EntityContextKey)entry.getKey()).getId(),
                  (EntityEnterpriseContext)entry.getValue());
         }
      }
   }
}
