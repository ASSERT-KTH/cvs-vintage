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
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.ListCacheKey;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCApplicationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCXmlFileLoader;
import org.jboss.logging.Logger;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.proxy.Proxy;
import org.jboss.util.CachePolicy;
import org.jboss.util.FinderResults;
import org.jboss.util.LRUCachePolicy;

import java.lang.reflect.Constructor;
import org.jboss.proxy.InvocationHandler;
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
 * @version $Revision: 1.17 $
 */
public class JDBCStoreManager implements EntityPersistenceStore {

   /**
    * To simplify null values handling in the preloaded data pool we use 
    * this value instead of 'null'
    */
   private static final Object NULL_VALUE = new Object();

   private Constructor beanProxyConstructor;

   private EntityContainer container;
   private Logger log;

   private JDBCEntityMetaData metaData;
   private JDBCEntityBridge entityBridge;

   private JDBCTypeFactory typeFactory;
   private JDBCQueryManager queryManager;

   private JDBCCommandFactory commandFactory;

   // Manager life cycle commands
   private JDBCInitCommand initCommand;
   private JDBCStartCommand startCommand;
   private JDBCStopCommand stopCommand;
   private JDBCDestroyCommand destroyCommand;

   // Entity life cycle commands
   private JDBCInitEntityCommand initEntityCommand;
   private JDBCFindEntityCommand findEntityCommand;
   private JDBCFindEntitiesCommand findEntitiesCommand;
   private JDBCCreateEntityCommand createEntityCommand;
   private JDBCRemoveEntityCommand removeEntityCommand;
   private JDBCLoadEntityCommand loadEntityCommand;
   private JDBCStoreEntityCommand storeEntityCommand;
   private JDBCActivateEntityCommand activateEntityCommand;
   private JDBCPassivateEntityCommand passivateEntityCommand;

   // commands
   private JDBCLoadFieldCommand loadFieldCommand;
   private JDBCFindByForeignKeyCommand findByForeignKeyCommand;
   private JDBCLoadRelationCommand loadRelationCommand;
   private JDBCDeleteRelationsCommand deleteRelationsCommand;
   private JDBCInsertRelationsCommand insertRelationsCommand;

   // read ahead stuff
   private boolean readAheadOnLoad;
   private int readAheadLimit;
   private JDBCReadAheadCommand readAheadCommand;
   private LRUCachePolicy readAheadCache;

   /**
    * A map of data preloaded within some transaction for some entity. This map
    * is keyed by Transaction, entityKey and CMP field name
    * and the data is Object containing the field value.
    */
   private Map preloadedData = new HashMap();

   /**
    * A set of transactions for which data was preloaded.
    */
   private Set transactions = new HashSet();

   /**
    * A Transaction manager so that we can link preloaded data to a transaction
    */
   private TransactionManager tm;

   public EntityContainer getContainer() {
      return container;
   }

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
      return (JDBCCommandFactory) commandFactory;
   }


   //
   // Store Manager Life Cycle Commands
   //
   public void init() throws Exception {
      log.debug("Initializing CMP plugin for " +
                container.getBeanMetaData().getEjbName());

      initTxDataMap();

      metaData = loadJDBCEntityMetaData();

      // setup the type factory, which is used to map java types to sql types.
      typeFactory = new JDBCTypeFactory(
            metaData.getTypeMapping(), 
            metaData.getJDBCApplication().getValueClasses());

      // create the bridge between java land and this engine (sql land)
      entityBridge = new JDBCEntityBridge(metaData, this);

      // Set up Commands
      commandFactory = new JDBCCommandFactory(this);

      // Create store manager life cycle commands
      initCommand = commandFactory.createInitCommand();
      startCommand = commandFactory.createStartCommand();
      stopCommand = commandFactory.createStopCommand();
      destroyCommand = commandFactory.createDestroyCommand();

      /// Create ejb life cycle commands
      initEntityCommand = commandFactory.createInitEntityCommand();
      findEntityCommand = commandFactory.createFindEntityCommand();
      findEntitiesCommand = commandFactory.createFindEntitiesCommand();
      createEntityCommand = commandFactory.createCreateEntityCommand();
      removeEntityCommand = commandFactory.createRemoveEntityCommand();
      loadFieldCommand = commandFactory.createLoadFieldCommand();
      loadEntityCommand = commandFactory.createLoadEntityCommand();
      storeEntityCommand = commandFactory.createStoreEntityCommand();
      activateEntityCommand = commandFactory.createActivateEntityCommand();
      passivateEntityCommand = commandFactory.createPassivateEntityCommand();

      // Create relationship commands
      findByForeignKeyCommand = commandFactory.createFindByForeignKeyCommand();
      loadRelationCommand = commandFactory.createLoadRelationCommand();
      deleteRelationsCommand = commandFactory.createDeleteRelationsCommand();
      insertRelationsCommand = commandFactory.createInsertRelationsCommand();

      // Initialize the read ahead code
      readAheadCommand = commandFactory.createReadAheadCommand();
      readAheadOnLoad = metaData.getReadAhead().isOnLoadUsed();
      if (readAheadOnLoad) {
         readAheadLimit = metaData.getReadAhead().getLimit();
         readAheadCache = 
               new LRUCachePolicy(2, metaData.getReadAhead().getCacheSize());
         readAheadCache.init();
      }
      tm = (TransactionManager) container.getTransactionManager();

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
      
      // If we are using a readAheadCache, start it.
      if(readAheadCache != null) {
         readAheadCache.start();
      }

      //
      // get the bean proxy constructor
      //

      // use proxy generator to create one implementation
      Class beanClass = container.getBeanClass();
      Class[] classes = new Class[] { beanClass };
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(
            container, 
            entityBridge,
            beanClass);
      ClassLoader classLoader = beanClass.getClassLoader();
      Object o = Proxy.newProxyInstance(classLoader, classes, handler);

      // steal the constructor from the object
      beanProxyConstructor = 
            o.getClass().getConstructor(new Class[]{InvocationHandler.class});
      
      // now create one to make sure everything is cool
      createBeanClassInstance();
   }

   public void stop() {
      // On deploy errors, sometimes CMPStoreManager was never initialized!
      if(stopCommand != null) { 
         stopCommand.execute();
      }

      // Inform the readAhead cache that we are done.
      if(readAheadCache != null) {
         readAheadCache.stop();
      }
   }

   public void destroy() {
      // On deploy errors, sometimes CMPStoreManager was never initialized!
      if(destroyCommand != null) {
         destroyCommand.execute();
      }

      if (readAheadCache != null) {
         readAheadCache.stop();
         readAheadCache = null;
      }
   }

   //
   // EJB Life Cycle Commands
   //
   /**
    * Returns a new instance of a class which implemnts the bean class.
    *
    * @see java.lang.Class#newInstance
    * @return the new instance
    */
   public Object createBeanClassInstance() throws Exception {
      Class beanClass = container.getBeanClass();

      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(
            container, 
            entityBridge,
            beanClass);

      return beanProxyConstructor.newInstance(new Object[]{handler});
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
         entityBridge.resetPersistenceContext(ctx);
      }

      if(readAheadOnLoad) {
         JDBCCMPFieldBridge[] fieldsToLoad = entityBridge.getEagerLoadFields();
         if((fieldsToLoad.length == 0) || readAheadFields(fieldsToLoad, ctx)) {
           return;
         }
      }
      
      loadEntityCommand.execute(ctx);
   }

   public void loadField(
         JDBCCMPFieldBridge field, EntityEnterpriseContext ctx) {

      JDBCCMPFieldBridge[] fieldsToLoad = 
            loadFieldCommand.getFieldGroupsUnion(field);

      if(readAheadOnLoad) {
         if(readAheadFields(fieldsToLoad, ctx)) {
            return;
         }
      }
      loadFieldCommand.execute(fieldsToLoad, ctx);
   }

   public void storeEntity(EntityEnterpriseContext ctx) {
      storeEntityCommand.execute(ctx);
      synchronizeRelationData(ctx.getTransaction());
   }

   public void synchronizeRelationData(Transaction tx) {
      Map txDataMap = getTxDataMap();
      Map txData = (Map)txDataMap.get(tx);
      if(txData != null) {
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
   public Set findByForeignKey(
         Object foreignKey, 
         JDBCCMPFieldBridge[] foreignKeyFields) {

      return findByForeignKeyCommand.execute(foreignKey, foreignKeyFields);
   }

   public Set loadRelation(JDBCCMRFieldBridge cmrField, Object pk) {
      return loadRelationCommand.execute(cmrField, pk);
   }

   public void deleteRelations(RelationData relationData) {
      deleteRelationsCommand.execute(relationData);
   }

   public void insertRelations(RelationData relationData) {  
      insertRelationsCommand.execute(relationData);
   }

   public Map getTxDataMap() {
      ApplicationMetaData amd = 
            container.getBeanMetaData().getApplicationMetaData();

      // Get Tx Hashtable
      return (Map)amd.getPluginData("CMP-JDBC-TX-DATA");
   }

   private void initTxDataMap() {
      ApplicationMetaData amd = 
            container.getBeanMetaData().getApplicationMetaData();

      // Get Tx Hashtable
      Map txDataMap = (Map)amd.getPluginData("CMP-JDBC-TX-DATA");
      if(txDataMap == null) {
         // we are the first JDBC CMP manager to get to initTxDataMap.
         txDataMap = Collections.synchronizedMap(new HashMap());
         amd.addPluginData("CMP-JDBC-TX-DATA", txDataMap);
      }
   }


   //
   // Read Ahead Code
   //
   
   /**
    * Fills all preloaded fields.
    * @return The list of fields that are not preloaded yet.
    */
   private JDBCCMPFieldBridge[] fillFromPreloaded(JDBCCMPFieldBridge[] fields, EntityEnterpriseContext ctx) {
      ArrayList notPreloaded = null;
      JDBCCMPFieldBridge[] fieldsToLoad;
      Object[] fieldValueRef;
      Object id;
      boolean found;

      id = ctx.getId();

      fieldValueRef = new Object[1];
      for (int i = 0; i < fields.length; i++) {
         found = getPreloadData(id, fields[i], fieldValueRef);
         if (found) {
            fields[i].setInstanceValue(ctx, fieldValueRef[0]);
            fields[i].setClean(ctx);
         } else {
            if (notPreloaded == null) {
               notPreloaded = new ArrayList();
            }
            notPreloaded.add(fields[i]);
         }
      }
      return (notPreloaded == null ? null :
            (JDBCCMPFieldBridge[]) notPreloaded.toArray(new JDBCCMPFieldBridge[notPreloaded.size()]));
   }

   private boolean readAheadFields(JDBCCMPFieldBridge[] fields, EntityEnterpriseContext ctx) {
      JDBCCMPFieldBridge[] fieldsToLoad;
      ListCacheKey key;
      boolean success = false;
      FinderResults results;

      //first check to see if the data was preloaded
      fieldsToLoad = fillFromPreloaded(fields, ctx);
      if (fieldsToLoad == null) {
         success = true;
      } else if (ctx.getCacheKey() instanceof ListCacheKey) {
         key = (ListCacheKey) ctx.getCacheKey();
         results = (FinderResults) readAheadCache.get(new Long(key.getListId()));
         if (results != null) {
            try {
               readAheadCommand.execute(fieldsToLoad, results, key.getIndex(),
                                        Math.min(results.size(), key.getIndex() + readAheadLimit));
               fieldsToLoad = fillFromPreloaded(fields, ctx);
               if (fieldsToLoad == null) {
                  success = true;
               } else {
                  log.warn("Didn't read ahead field '" + fieldsToLoad[0].getMetaData().getFieldName() + "'");
                  success = false;
               }
            } catch(EJBException e) {
               log.warn("Read ahead failed", e);
            }
         }
      }
      return success;
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

   public CachePolicy getReadAheadCache() {
      return readAheadCache;
   }

   /**
    * Add preloaded data for an entity within the scope of a transaction
    */
   void addPreloadData(
         Object entityKey,
         JDBCCMPFieldBridge field,
         Object fieldValue)
   {
      Transaction trans = null;
      PreloadKey preloadKey;

      try {
         trans = tm.getTransaction();
      } catch (javax.transaction.SystemException sysE) {
         log.warn("System exception getting transaction for preload - " +
               "can't preload data for " + entityKey, sysE);
         return;
      }

      synchronized (transactions) { 
         if (trans != null && !transactions.contains(trans)) {
            if (!transactions.contains(trans)) {
               try {
                  trans.registerSynchronization(new PreloadClearSynch(trans));
               } catch (javax.transaction.SystemException se) {
                  log.warn("System exception getting transaction for " +
                        "preload - can't get preloaded data for " + 
                        entityKey, se);
                  return;
               } catch (javax.transaction.RollbackException re) {
                  log.warn("Rollback exception getting transaction for " + 
                        "preload - can't get preloaded data for " + 
                        entityKey, re);
                  return;
               }
               transactions.add(trans);
            }
         }
      }
      preloadKey = new PreloadKey(
            trans, 
            entityKey, 
            field.getMetaData().getFieldName());
      preloadedData.put(
            preloadKey, 
            (fieldValue == null ? NULL_VALUE : fieldValue));
   }

   /**
    * Get data that we might have preloaded for an entity in a transaction
    * @param fieldValueRef will be filled with the field value
    * @return whether the data was found in the pool (null field value doesn't
    * mean that it wasn't).
    */
   private boolean getPreloadData(
         Object entityKey, 
         JDBCCMPFieldBridge field,
         Object[] fieldValueRef)
   {
      Transaction trans = null;
      PreloadKey preloadKey;
      Object fieldValue;
      boolean found;

      try {
         trans = tm.getTransaction();
      } catch (javax.transaction.SystemException sysE) {
         log.warn("System exception getting transaction for preload - not " +
               "preloading " + entityKey, sysE);
         return false;
      }

      preloadKey = new PreloadKey(
            trans, entityKey, field.getMetaData().getFieldName());
      fieldValue = preloadedData.remove(preloadKey);
      log.debug("Getting Preload " + preloadKey + " " + 
            field.getMetaData().getFieldName() + " " + fieldValue);
      found = (fieldValue != null);


      // due to this trick we avoid synchronization on preloadedData
      if (fieldValue == NULL_VALUE) { 
         fieldValue = null;
      }
      fieldValueRef[0] = fieldValue;
      return found;
   }

   /**
    * Clear out any data we have preloaded for any entity in this transaction
    */
   private void clearPreloadForTrans(Transaction trans)
   {
      Map.Entry entry;
      PreloadKey preloadKey;

      if(transactions.remove(trans)) {
         for(Iterator it = preloadedData.entrySet().iterator(); it.hasNext();) {
            entry = (Map.Entry) it.next();
            preloadKey = (PreloadKey) entry.getKey();
            if (preloadKey.trans == trans) {
               it.remove();
            }
         }
      }
   }

   /** Inner class used in the preload Data hashmaps so that we can wrap a
    *  SoftReference around the data and still have enough information to remove
    *  the reference from the appropriate hashMap.
    */
   private class PreloadKey {
      final Object key;
      final Transaction trans;
      final String field;

      PreloadKey(Transaction trans, Object key, String field) {
         this.trans = trans;
         this.key = key;
         this.field = field;
      }

      public boolean equals(Object obj) {
         PreloadKey preloadKey = (PreloadKey) obj;

         return ((trans == preloadKey.trans) &&
                     field.equals(preloadKey.field) &&
               key.equals(preloadKey.key));
      }

      public int hashCode() {
         return (
               key.hashCode() +
               field.hashCode() +
               (trans == null ? 0 : trans.hashCode()));
      }
   }

   private class PreloadClearSynch 
         implements javax.transaction.Synchronization {

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
