/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.DeploymentException;

import org.jboss.ejb.plugins.jaws.JPMCommandFactory;
import org.jboss.ejb.plugins.jaws.JPMInitCommand;
import org.jboss.ejb.plugins.jaws.JPMStartCommand;
import org.jboss.ejb.plugins.jaws.JPMStopCommand;
import org.jboss.ejb.plugins.jaws.JPMDestroyCommand;
import org.jboss.ejb.plugins.jaws.JPMFindEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMFindEntitiesCommand;
import org.jboss.ejb.plugins.jaws.JPMCreateEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMRemoveEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMLoadEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMLoadEntitiesCommand;
import org.jboss.ejb.plugins.jaws.JPMStoreEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMActivateEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMPassivateEntityCommand;

import org.jboss.metadata.ApplicationMetaData;

import org.jboss.ejb.plugins.jaws.metadata.JawsXmlFileLoader;
import org.jboss.ejb.plugins.jaws.metadata.JawsEntityMetaData;
import org.jboss.ejb.plugins.jaws.metadata.JawsApplicationMetaData;
import org.jboss.ejb.plugins.jaws.metadata.FinderMetaData;

import org.jboss.logging.Log;
import org.jboss.util.FinderResults;

/**
 * JAWSPersistenceManager JDBCCommandFactory
 *
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.9 $
 */
public class JDBCCommandFactory implements JPMCommandFactory
{
   // Attributes ----------------------------------------------------
   
   private EntityContainer container;
   private Context javaCtx;
   private JawsEntityMetaData metadata;
   private Log log;
   private boolean debug = false;
   
   /** a map of data preloaded within some transaction for some entity. This map
    *  is keyed by Transaction and the data are hashmaps with key = entityKey and
    *  data = Object[] containing the entity data. 
    *  @todo use weak references to ease memory. */
   private Map preloadedData = new HashMap();
   /** A map of data preloaded without a transaction context. key=entityKey, 
    *  data = Object[] containing entity data
    *  @todo use weak references to ease memory. 
    */
   private Map nonTransactionalPreloadData = new HashMap();
   
   /** a Transaction manager so that we can link preloaded data to a transaction */
   private TransactionManager tm;
   
   // These support singletons (within the scope of this factory)
   private JDBCBeanExistsCommand beanExistsCommand;
   private JPMFindEntitiesCommand findEntitiesCommand;
   
   // Constructors --------------------------------------------------
   
   public JDBCCommandFactory(EntityContainer container,
                             Log log)
      throws Exception
   {
      this.container = container;
      this.log = log;
	  
      this.javaCtx = (Context)new InitialContext().lookup("java:comp/env");
      
	  String ejbName = container.getBeanMetaData().getEjbName();
	  ApplicationMetaData amd = container.getBeanMetaData().getApplicationMetaData();
	  JawsApplicationMetaData jamd = (JawsApplicationMetaData)amd.getPluginData("JAWS");
	  
	  if (jamd == null) {
	     // we are the first cmp entity to need jaws. Load jaws.xml for the whole application
		 JawsXmlFileLoader jfl = new JawsXmlFileLoader(amd, container.getClassLoader(), container.getLocalClassLoader(), log);
       jamd = jfl.load();
		 amd.addPluginData("JAWS", jamd);
	  }
     debug = jamd.getDebug();
		  
	  metadata = jamd.getBeanByEjbName(ejbName);
	  if (metadata == null) {
		  throw new DeploymentException("No metadata found for bean " + ejbName);
	  }
      
     tm = (TransactionManager) container.getTransactionManager();
   }
   
   // Public --------------------------------------------------------
   
   public EntityContainer getContainer()
   {
      return container;
   }
   
   public Context getJavaCtx()
   {
      return javaCtx;
   }
   
   public JawsEntityMetaData getMetaData()
   {
      return metadata;
   }
   
   public Log getLog()
   {
      return log;
   }
   
   public boolean getDebug() 
   {
      return debug;
   }
   
   // Additional Command creation
   
   /**
    * Singleton: multiple callers get references to the 
    * same command instance.
    */
   public JDBCBeanExistsCommand createBeanExistsCommand()
   {
      if (beanExistsCommand == null)
      {
         beanExistsCommand = new JDBCBeanExistsCommand(this);
      }
      
      return beanExistsCommand;
   }
   
   public JPMFindEntitiesCommand createFindAllCommand(FinderMetaData f)
   {
      return new JDBCFindAllCommand(this, f);
   }
   
   public JPMFindEntitiesCommand createDefinedFinderCommand(FinderMetaData f)
   {
      return new JDBCDefinedFinderCommand(this, f);
   }
   
   public JPMFindEntitiesCommand createFindByCommand(Method finderMethod, FinderMetaData f)
      throws IllegalArgumentException
   {
      return new JDBCFindByCommand(this, finderMethod, f);
   }
   
   // JPMCommandFactory implementation ------------------------------
   
   // lifecycle commands
   
   public JPMInitCommand createInitCommand()
   {
      return new JDBCInitCommand(this);
   }
   
   public JPMStartCommand createStartCommand()
   {
      return new JDBCStartCommand(this);
   }
   
   public JPMStopCommand createStopCommand()
   {
      return new JDBCStopCommand(this);
   }
   
   public JPMDestroyCommand createDestroyCommand()
   {
      return new JDBCDestroyCommand(this);
   }
   
   // entity persistence-related commands
   
   public JPMFindEntityCommand createFindEntityCommand()
   {
      return new JDBCFindEntityCommand(this);
   }
   
   /**
    * Singleton: multiple callers get references to the 
    * same command instance.
    */
   public JPMFindEntitiesCommand createFindEntitiesCommand()
   {
      if (findEntitiesCommand == null)
      {
         findEntitiesCommand = new JDBCFindEntitiesCommand(this);
      }
      
      return findEntitiesCommand;
   }
   
   public JPMCreateEntityCommand createCreateEntityCommand()
   {
      return new JDBCCreateEntityCommand(this);
   }
   
   public JPMRemoveEntityCommand createRemoveEntityCommand()
   {
      return new JDBCRemoveEntityCommand(this);
   }
   
   public JPMLoadEntityCommand createLoadEntityCommand()
   {
      return new JDBCLoadEntityCommand(this);
   }
   
   public JPMLoadEntitiesCommand createLoadEntitiesCommand()
   {
      return new JDBCLoadEntitiesCommand(this);
   }
   
   public JPMStoreEntityCommand createStoreEntityCommand()
   {
      return new JDBCStoreEntityCommand(this);
   }
   
   // entity activation and passivation commands
   
   public JPMActivateEntityCommand createActivateEntityCommand()
   {
      return new JDBCActivateEntityCommand(this);
   }
   
   public JPMPassivateEntityCommand createPassivateEntityCommand()
   {
      return new JDBCPassivateEntityCommand(this);
   }
   
   
   /** Add preloaded data for an entity within the scope of a transaction */
   /*package*/ void addPreloadData(Object entityKey, Object[] entityData) 
   {
      Transaction trans = null;
      try {
         trans = tm.getTransaction();
      } catch (javax.transaction.SystemException sysE) {
         log.warning("System exception getting transaction for preload - can't get preloaded data for "+entityKey);
         return;
      }
//log.debug("PRELOAD: adding preload for "+entityKey+" in transaction "+(trans != null ? trans.toString() : "NONE"));
      
      if (trans != null) {
         synchronized (preloadedData) {
            Map entitiesInTransaction = (Map)preloadedData.get(trans);
            if (entitiesInTransaction == null) {
               try {
                  trans.registerSynchronization(new PreloadClearSynch(trans));
               } catch (javax.transaction.SystemException se) {
                  log.warning("System exception getting transaction for preload - can't get preloaded data for "+entityKey);
                  return;
               } catch (javax.transaction.RollbackException re) {
                  log.warning("Rollback exception getting transaction for preload - can't get preloaded data for "+entityKey);
                  return;
               }
               entitiesInTransaction = new HashMap();
               preloadedData.put(trans, entitiesInTransaction);
            }
            entitiesInTransaction.put(entityKey, entityData);
         }
      } else {
         synchronized (nonTransactionalPreloadData) {
            nonTransactionalPreloadData.put(entityKey, entityData);
         }
      }
   }
   
   /** get data that we might have preloaded for an entity in a transaction - 
    *  may return null!
    */
   /*package*/ Object[] getPreloadData(Object entityKey) 
   {
      Transaction trans = null;
      try {
         trans = tm.getTransaction();
      } catch (javax.transaction.SystemException sysE) {
         log.warning("System exception getting transaction for preload - not preloading "+entityKey);
         return null;
      }
      
      Object[] result = null;
      if (trans != null) {
         synchronized (preloadedData) {
            Map entitiesInTransaction = (Map)preloadedData.get(trans);
            if (entitiesInTransaction != null)
               result = (Object[])entitiesInTransaction.get(entityKey);
            //remove it now?
         }
      } else {
         synchronized (nonTransactionalPreloadData) {
            result = (Object[])nonTransactionalPreloadData.get(entityKey);
         }
      }
//log.debug("PRELOAD: returning "+result+" as preload for "+entityKey);
      return result;
   }
   
   /** clear out any data we have preloaded for any entity in this transaction */
   /*package*/ void clearPreloadForTrans(Transaction trans) 
   {
//log.debug("PRELOAD: clearing preload for transaction "+trans.toString());
      synchronized (preloadedData) {
         preloadedData.remove(trans);
      }
   }
   
   
   // Private -------------------------------------------------------
   /** an inner class used to key the FinderResults by their finder method and
    *  the transaction they're invoked within
    */
   private static class PreloadDataKey {
      private Object entityKey;
      private Transaction transaction;
      
      private int hashCode;
      
      public PreloadDataKey(Object entityKey, Transaction transaction) {
         this.entityKey = entityKey;
         this.transaction = transaction;
         
         //accumulate the hashcode. 
         /** @todo investigate ways of combining these that will give the least collisions */
         this.hashCode = entityKey.hashCode();
         if (transaction != null)
            this.hashCode += transaction.hashCode();
      }
      
      public int hashCode() {
         return hashCode;
      }
      public boolean equals(Object o) {
         if (o instanceof PreloadDataKey) {
            PreloadDataKey other = (PreloadDataKey)o;
            return (other.entityKey.equals(this.entityKey)) &&
                   ( (other.transaction == null && this.transaction == null) ||
                     ( (other.transaction != null && this.transaction != null) &&
                       (other.transaction.equals(this.transaction)) ) );
         }
         return false;
      }
   }
   
   private class PreloadClearSynch implements javax.transaction.Synchronization{
      private Transaction forTrans;
      public PreloadClearSynch(Transaction forTrans) {
         this.forTrans = forTrans;
      }
      public void afterCompletion(int p0) {
         clearPreloadForTrans(forTrans);
      }
      public void beforeCompletion() {
         //no-op
      }
   }
}
