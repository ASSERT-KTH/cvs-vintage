/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.WeakHashMap;

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
import org.jboss.util.TimerTask;
import org.jboss.util.TimerQueue;

/**
 * Command factory for the JAWS JDBC layer. This class is primarily responsible
 * for creating instances of the JDBC implementations for the various JPM 
 * commands so that the JAWSPersistenceManager (actually an persistence store)
 * can delegate to them in a decoupled manner.
 * <p>This class also acts as the manager for the read-ahead buffer added in 
 * version 2.3/2.4. In order to manage this buffer, it must register itself
 * with any transaction that is active when a finder is called so that the 
 * data that was read ahead can be discarded before completion of the 
 * transaction. The read ahead buffer is managed using Soft references, with
 * a ReferenceQueue being used to tell when the VM has garbage collected an
 * object so that we can keep the hashtables clean.
 *
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.10 $
 */
public class JDBCCommandFactory implements JPMCommandFactory
{
   // Attributes ----------------------------------------------------
   
   private EntityContainer container;
   private Context javaCtx;
   private JawsEntityMetaData metadata;
   private Log log;
   private boolean debug = false;

   /** Timer queue used to time polls on the preloadRefQueue on all JAWS 
    *  handled entities
    */
   private static TimerQueue softRefHandler;
   
   /** Timer queue used to get references to preload data who've been GC'ed */
   private ReferenceQueue preloadRefQueue = new ReferenceQueue();
   
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

   //static initializer to kick off our softRefhandler 
   static {
      softRefHandler = new TimerQueue("JAWS Preload reference handler");
      softRefHandler.start();
   }
      
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
      
      softRefHandler.schedule(new PreloadRefQueueHandlerTask(), 50);
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
         log.warning("System exception getting transaction for preload - can't preload data for "+entityKey);
         return;
      }
//log.debug("PRELOAD: adding preload for "+entityKey+" in transaction "+(trans != null ? trans.toString() : "NONE")+" entityData="+entityData);
      
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
            PreloadData preloadData = new PreloadData(trans, entityKey, entityData, preloadRefQueue);
            entitiesInTransaction.put(entityKey, preloadData);
         }
      } else {
         synchronized (nonTransactionalPreloadData) {
            PreloadData preloadData = new PreloadData(null, entityKey, entityData, preloadRefQueue);
            nonTransactionalPreloadData.put(entityKey, preloadData);
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
      PreloadData preloadData = null;
      if (trans != null) {
         Map entitiesInTransaction = null;
         // Do we really need this to be syncrhonized? What is the effect of 
         //    another thread trying to modify this map? It won't be to remove
         //    our transaction (we're in it here!, trying to call a business 
         //    method), and who cares if another is added/removed?
//         synchronized (preloadedData) {
            entitiesInTransaction = (Map)preloadedData.get(trans);
//         }
         if (entitiesInTransaction != null) {
            synchronized (entitiesInTransaction) {
               preloadData = (PreloadData)entitiesInTransaction.get(entityKey);
               entitiesInTransaction.remove(entityKey);
            }
         }
      } else {
         synchronized (nonTransactionalPreloadData) {
            preloadData = (PreloadData)nonTransactionalPreloadData.get(entityKey);
            nonTransactionalPreloadData.remove(entityKey);
         }
      }
      if (preloadData != null) {
         result = preloadData.getData();
      } /*else {
         log.debug("PRELOAD: preloadData == null for "+entityKey);
      }
if (result == null)
   log.debug("PRELOAD: returning null as preload for "+entityKey);
   */
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
   
   /** Inner class that handles our reference queue. I didn't think this would 
    *  be neccessary, but for some reason the VM won't call an override of 
    *  Reference.clear() 
   */
   private class PreloadRefQueueHandlerTask extends TimerTask {
   	public void execute() throws Exception {
         PreloadData preloadData = (PreloadData)preloadRefQueue.poll();
         int handled = 0;
         while (preloadData != null && handled < 10) {
            log.debug("PRELOAD: clearing "+preloadData.getKey());
            if (preloadData.getTransaction() != null) {
               Map entitiesInTransaction = null;
               // Do we really need this to be syncrhonized? What is the effect of 
               //    another thread trying to modify this map? It won't be to remove
               //    our transaction (we're in it here!, trying to call a business 
               //    method), and who cares if another is added/removed?
      //         synchronized (preloadedData) {
                  entitiesInTransaction = (Map)preloadedData.get(preloadData.getTransaction());
      //         }
               if (entitiesInTransaction != null) {
                  synchronized (entitiesInTransaction) {
                     entitiesInTransaction.remove(preloadData.getKey());
                  }
               }
            } else {
               synchronized (nonTransactionalPreloadData) {
                  nonTransactionalPreloadData.remove(preloadData.getKey());
               }
            }
            preloadData.empty();
            handled++;
            
            preloadData = (PreloadData)preloadRefQueue.poll();
         }
      }
   }
   /** Inner class used in the preload Data hashmaps so that we can wrap a 
    *  SoftReference around the data and still have enough information to remove
    *  the reference from the appropriate hashMap.
    */
   private class PreloadData extends SoftReference {
      private Object key;
      private Transaction trans;
      
      PreloadData(Transaction trans, Object key, Object[] data, ReferenceQueue queue) {
         super(data, queue);
         this.trans = trans;
         this.key = key;
      }
      
      Transaction getTransaction() {
         return trans;
      }
      Object getKey() {
         return key;
      }
      Object[] getData() {
         return (Object[])get();
      }
      
      /** Named empty to not collide with superclass clear */
      public void empty() {
         key = null;
         trans = null;
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
