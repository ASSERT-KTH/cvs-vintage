/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.ListCacheKey;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCApplicationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCXmlFileLoader;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.util.CachePolicy;
import org.jboss.ejb.FinderResults;
import org.jboss.util.LRUCachePolicy;

/**
 * JDBCStoreManager manages storage of persistence data into a table.
 * Other then loading the initial jbosscmp-jdbc.xml file this class
 * does very little. The interesting tasks are performed by the command
 * classes.
 *
 * Life-cycle:
 *      Tied to the life-cycle of the entity container.
 *
 * Multiplicity:
 *      One per cmp entity bean. This could be less if another implementaion of
 * EntityPersistenceStore is created and thoes beans use the implementation
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @see org.jboss.ejb.EntityPersistenceStore
 * @version $Revision: 1.28 $
 */
public class JDBCStoreManager implements EntityPersistenceStore {

   /**
    * The key used to store the tx data map.
    */
   private static final Object TX_DATA_KEY = "TX_DATA_KEY";

   private static final Map applicationData = 
         Collections.synchronizedMap(new HashMap());

   private EntityContainer container;
   private Logger log;

   private JDBCEntityMetaData metaData;
   private JDBCEntityBridge entityBridge;

   private JDBCTypeFactory typeFactory;
   private JDBCQueryManager queryManager;

   private JDBCCommandFactory commandFactory;

   private ReadAheadCache readAheadCache;

   // Manager life cycle commands
   private JDBCInitCommand initCommand;
   private JDBCStartCommand startCommand;
   private JDBCStopCommand stopCommand;
   private JDBCDestroyCommand destroyCommand;

   // Entity life cycle commands
   private JDBCCreateBeanClassInstanceCommand createBeanClassInstanceCommand;
   private JDBCInitEntityCommand initEntityCommand;
   private JDBCFindEntityCommand findEntityCommand;
   private JDBCFindEntitiesCommand findEntitiesCommand;
   private JDBCCreateEntityCommand createEntityCommand;
   private JDBCRemoveEntityCommand removeEntityCommand;
   private JDBCLoadEntityCommand loadEntityCommand;
   private JDBCIsModifiedCommand isModifiedCommand;
   private JDBCStoreEntityCommand storeEntityCommand;
   private JDBCActivateEntityCommand activateEntityCommand;
   private JDBCPassivateEntityCommand passivateEntityCommand;

   // commands
   private JDBCLoadRelationCommand loadRelationCommand;
   private JDBCDeleteRelationsCommand deleteRelationsCommand;
   private JDBCInsertRelationsCommand insertRelationsCommand;

   /**
    * A Transaction manager so that we can link preloaded data to a transaction
    */
   private TransactionManager tm;

   /**
    * Gets the container for this entity.
    * @return the container for this entity; null if container has not been set
    */
   public EntityContainer getContainer() {
      return container;
   }

   /**
    * Sets the container for this entity.
    * @param container the container for this entity
    * @throws ClassCastException if the container is not an instance of 
    * EntityContainer
    */
   public void setContainer(Container container) {
      this.container = (EntityContainer)container;
      this.log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            container.getBeanMetaData().getEjbName());
   }
 
   public JDBCEntityBridge getEntityBridge() {
      return entityBridge;
   }

   public JDBCTypeFactory getJDBCTypeFactory() {
      return typeFactory;
   }

   public JDBCEntityMetaData getMetaData() {
      return metaData;
   }

   public JDBCQueryManager getQueryManager() {
      return queryManager;
   }

   public JDBCCommandFactory getCommandFactory() {
      return commandFactory;
   }
   
   public ReadAheadCache getReadAheadCache() {
      return readAheadCache;
   }
   
   //
   // Genertic data containers
   //
   public Map getApplicationDataMap() {
      return applicationData;
   }

   public Object getApplicationData(Object key) {
      return applicationData.get(key);
   }

   public void putApplicationData(Object key, Object value) {
      applicationData.put(key, value);
   }

   public void removeApplicationData(Object key) {
      applicationData.remove(key);
   }

   public Map getApplicationTxDataMap() {
      try {
         Transaction tx = tm.getTransaction();
      
         // get the map between the tx and the txDataMap
         Map txMap = (Map)getApplicationData(TX_DATA_KEY);
         synchronized(txMap) {
            // get the txDataMap from the txMap
            Map txDataMap = (Map)txMap.get(tx);

            // do we have an existing map
            if(txDataMap == null) {
               // We want to be notified when the transaction commits
               ApplicationTxDataSynchronization synch = 
                     new ApplicationTxDataSynchronization(tx);
               tx.registerSynchronization(synch);

               // create and add the new map
               txDataMap = new HashMap();
               txMap.put(tx, txDataMap);
            }
            return txDataMap;
         }
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Error getting application tx data map.", e);
      }
   }

   public Object getApplicationTxData(Object key) {
      return getApplicationTxDataMap().get(key);
   }

   public void putApplicationTxData(Object key, Object value) {
      getApplicationTxDataMap().put(key, value);
   }

   public void removeApplicationTxData(Object key) {
      getApplicationTxDataMap().remove(key);
   }

   public Map getEntityTxDataMap() {
      Map entityTxDataMap = (Map)getApplicationTxData(this);
      if(entityTxDataMap == null) {
         entityTxDataMap = new HashMap();
         putApplicationTxData(this, entityTxDataMap);
      }
      return entityTxDataMap;
   }

   public Object getEntityTxData(Object key) {
      return getEntityTxDataMap().get(key);
   }

   public void putEntityTxData(Object key, Object value) {
      getEntityTxDataMap().put(key, value);
   }

   public void removeEntityTxData(Object key) {
      getEntityTxDataMap().remove(key);
   }

   private void initApplicationDataMap() {
      synchronized(applicationData) {
         Map txDataMap = (Map)getApplicationData(TX_DATA_KEY);
         if(txDataMap == null) {
            txDataMap = new HashMap();
            putApplicationData(TX_DATA_KEY, txDataMap);
         }
      }
   }

   //
   // Store Manager Life Cycle Commands
   //
   public void create() throws Exception {
      log.debug("Initializing CMP plugin for " +
                container.getBeanMetaData().getEjbName());

      // initializes the generic data containers
      initApplicationDataMap();

      // load the metadata for this entity
      metaData = loadJDBCEntityMetaData();

      // get the transaction manager
      tm = container.getTransactionManager();

      // setup the type factory, which is used to map java types to sql types.
      typeFactory = new JDBCTypeFactory(
            metaData.getTypeMapping(), 
            metaData.getJDBCApplication().getValueClasses());

      // create the bridge between java land and this engine (sql land)
      entityBridge = new JDBCEntityBridge(metaData, this);

      // add the entity bridge to the catalog
      Catalog catalog = (Catalog)getApplicationData("CATALOG");
      if(catalog == null) {
         catalog = new Catalog();
         putApplicationData("CATALOG", catalog);
      }
      catalog.addEntity(entityBridge);

      // create the read ahead cache
      readAheadCache = new ReadAheadCache(this);
      readAheadCache.create();

      // Set up Commands
      commandFactory = new JDBCCommandFactory(this);

      // Create store manager life cycle commands
      initCommand = commandFactory.createInitCommand();
      startCommand = commandFactory.createStartCommand();
      stopCommand = commandFactory.createStopCommand();
      destroyCommand = commandFactory.createDestroyCommand();

      /// Create ejb life cycle commands
      createBeanClassInstanceCommand = 
            commandFactory.createCreateBeanClassInstanceCommand();
      initEntityCommand = commandFactory.createInitEntityCommand();
      findEntityCommand = commandFactory.createFindEntityCommand();
      findEntitiesCommand = commandFactory.createFindEntitiesCommand();
      createEntityCommand = commandFactory.createCreateEntityCommand();
      removeEntityCommand = commandFactory.createRemoveEntityCommand();
      loadEntityCommand = commandFactory.createLoadEntityCommand();
      isModifiedCommand = commandFactory.createIsModifiedCommand();
      storeEntityCommand = commandFactory.createStoreEntityCommand();
      activateEntityCommand = commandFactory.createActivateEntityCommand();
      passivateEntityCommand = commandFactory.createPassivateEntityCommand();

      loadRelationCommand = commandFactory.createLoadRelationCommand();
      deleteRelationsCommand = commandFactory.createDeleteRelationsCommand();
      insertRelationsCommand = commandFactory.createInsertRelationsCommand();

      // Create the query manager
      queryManager = new JDBCQueryManager(this);

      // Execute the init Command
      initCommand.execute();
   }

   public void start() throws Exception {
      startCommand.execute();
      
      // Start the query manager. At this point is creates all of the
      // query commands. The must occure in the start phase, as
      // queries can opperate on other entities in the application, and
      // all entities are gaurenteed to be createed until the start phase.
      queryManager.start();
      
      readAheadCache.start();
   }

   public void stop() {
      // On deploy errors, sometimes CMPStoreManager was never initialized!
      if(stopCommand != null) { 
         stopCommand.execute();
      }

      readAheadCache.stop();
   }

   public void destroy() {
      // On deploy errors, sometimes CMPStoreManager was never initialized!
      if(destroyCommand != null) {
         destroyCommand.execute();
      }

      readAheadCache.destroy();
      readAheadCache = null;
   }

   //
   // EJB Life Cycle Commands
   //
   /**
    * Returns a new instance of a class which implemnts the bean class.
    *
    * @return the new instance
    */
   public Object createBeanClassInstance() throws Exception {
      return createBeanClassInstanceCommand.execute();
   }

   public void initEntity(EntityEnterpriseContext ctx) {
      initEntityCommand.execute(ctx);
   }

   public Object createEntity(
         Method createMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws CreateException {

      return createEntityCommand.execute(createMethod, args, ctx);
   }

   public Object findEntity(
         Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws FinderException {

      return findEntityCommand.execute(finderMethod, args, ctx);
   }

   public FinderResults findEntities(
         Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws FinderException {
      return findEntitiesCommand.execute(finderMethod, args, ctx);
   }

   public void activateEntity(EntityEnterpriseContext ctx) {
      activateEntityCommand.execute(ctx);
   }

   public void loadEntity(EntityEnterpriseContext ctx) {
      // is any on the data already in the entity valid
      if(!ctx.isValid()) {
         if(log.isTraceEnabled()) {
            log.trace("RESET PERSISTENCE CONTEXT: id="+ctx.getId());
         }
         entityBridge.resetPersistenceContext(ctx);
      }

      loadEntityCommand.execute(ctx);
   }

   public void loadField(
         JDBCCMPFieldBridge field, EntityEnterpriseContext ctx) {

      loadEntityCommand.execute(field, ctx);
   }

   public boolean isModified(EntityEnterpriseContext ctx) {
      return isModifiedCommand.execute(ctx);
   }

   public void storeEntity(EntityEnterpriseContext ctx) {
      storeEntityCommand.execute(ctx);
      synchronizeRelationData();
   }

   private void synchronizeRelationData() {
      Map txData = getApplicationTxDataMap();
      Iterator iterator = txData.values().iterator();
      while(iterator.hasNext()) {
         Object obj = iterator.next();
         if(obj instanceof RelationData) {
            RelationData relationData = (RelationData) obj;
               
            // only need to bother if neither side has a foreign key
            if(!relationData.getLeftCMRField().hasForeignKey() &&
            !relationData.getRightCMRField().hasForeignKey()) {
                  
               // delete all removed pairs from relation table
               deleteRelations(relationData);
                  
               // insert all added pairs into the relation table
               insertRelations(relationData);
                  
               relationData.addedRelations.clear();
               relationData.removedRelations.clear();
               relationData.notRelatedPairs.clear();
            }
         }
      }
   }   

   public void passivateEntity(EntityEnterpriseContext ctx) {
      passivateEntityCommand.execute(ctx);
   }

   public void removeEntity(EntityEnterpriseContext ctx)
         throws RemoveException {
      removeEntityCommand.execute(ctx);
   }

   //
   // Relationship Commands
   //
   public Collection loadRelation(JDBCCMRFieldBridge cmrField, Object pk) {
      return loadRelationCommand.execute(cmrField, pk);
   }

   public void deleteRelations(RelationData relationData) {
      deleteRelationsCommand.execute(relationData);
   }

   public void insertRelations(RelationData relationData) {  
      insertRelationsCommand.execute(relationData);
   }

   private JDBCEntityMetaData loadJDBCEntityMetaData() 
         throws DeploymentException {

      ApplicationMetaData amd = 
            container.getBeanMetaData().getApplicationMetaData();

      // Get JDBC MetaData
      JDBCApplicationMetaData jamd = 
            (JDBCApplicationMetaData)amd.getPluginData("CMP-JDBC");

      if (jamd == null) {
         // we are the first cmp entity to need jbosscmp-jdbc. 
         // Load jbosscmp-jdbc.xml for the whole application
         JDBCXmlFileLoader jfl = new JDBCXmlFileLoader(
               amd, 
               container.getClassLoader(),
               container.getLocalClassLoader(),
               log);

         jamd = jfl.load();
         amd.addPluginData("CMP-JDBC", jamd);
      }

      // Get JDBC Bean MetaData
      String ejbName = container.getBeanMetaData().getEjbName();
      JDBCEntityMetaData metadata = jamd.getBeanByEjbName(ejbName);
      if(metadata == null) {
         throw new DeploymentException("No metadata found for bean " + ejbName);
      }
      return metadata;
   }

   private class ApplicationTxDataSynchronization implements Synchronization {
      /**
       *  The transaction we follow.
       */
      private Transaction tx;
      
      /**
       *  Create a new instance synchronization instance.
       */
      private ApplicationTxDataSynchronization(Transaction tx)
      {
         this.tx = tx;
      }
 
      /**
       * Unused
       */
      public void beforeCompletion() {
         //no-op
      }

      /**
       * Free-up any data associated with this transaction.
       */
      public void afterCompletion(int status) {
         Map txMap = (Map)getApplicationData(TX_DATA_KEY);
         synchronized(txMap) {
            txMap.remove(tx);
         }
      }
   }
}
