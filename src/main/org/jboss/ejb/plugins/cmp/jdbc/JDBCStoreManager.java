/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.ListCacheKey;
import org.jboss.ejb.plugins.cmp.CMPStoreManager;
import org.jboss.ejb.plugins.cmp.CommandFactory;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridgeInvocationHandler;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCApplicationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCXmlFileLoader;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.proxy.Proxy;
import org.jboss.util.CachePolicy;
import org.jboss.util.FinderResults;
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
 * @version $Revision: 1.11 $
 */
public class JDBCStoreManager extends CMPStoreManager {
   /**
    * To simplify null values handling in the preloaded data pool we use this value instead of 'null'
    */
   private static final Object NULL_VALUE = new Object();

   protected DataSource dataSource;

   protected JDBCTypeFactory typeFactory;
   protected boolean debug;

   protected JDBCEntityMetaData metaData;
   protected JDBCEntityBridge entityBridge;

   protected JDBCLoadFieldCommand loadFieldCommand;
   protected JDBCFindByForeignKeyCommand findByForeignKeyCommand;
   protected JDBCLoadRelationCommand loadRelationCommand;
   protected JDBCDeleteRelationsCommand deleteRelationsCommand;
   protected JDBCInsertRelationsCommand insertRelationsCommand;
   protected boolean readAheadOnLoad;
   protected int readAheadLimit;
   protected JDBCReadAheadCommand readAheadCommand;

   protected LRUCachePolicy readAheadCache;

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

   public void init() throws Exception {
      initTxDataMap();

      metaData = loadJDBCEntityMetaData();

      // set debug flag
      debug = metaData.isDebug();
      
      // find the datasource
      try {
         dataSource = (DataSource)new InitialContext().lookup(metaData.getDataSourceName());
      } catch(NamingException e) {
         throw new DeploymentException("Error: can't find data source: " + metaData.getDataSourceName());
      }

      typeFactory = new JDBCTypeFactory(metaData.getTypeMapping(), metaData.getJDBCApplication().getValueClasses());
      entityBridge = new JDBCEntityBridge(metaData, log, this);

      super.init();

      loadFieldCommand = getCommandFactory().createLoadFieldCommand();
      findByForeignKeyCommand = getCommandFactory().createFindByForeignKeyCommand();
      loadRelationCommand = getCommandFactory().createLoadRelationCommand();
      deleteRelationsCommand = getCommandFactory().createDeleteRelationsCommand();
      insertRelationsCommand = getCommandFactory().createInsertRelationsCommand();
      readAheadCommand = getCommandFactory().createReadAheadCommand();
      readAheadOnLoad = metaData.getReadAhead().isOnLoadUsed();
      if (readAheadOnLoad) {
         readAheadLimit = metaData.getReadAhead().getLimit();
         readAheadCache = new LRUCachePolicy(2, metaData.getReadAhead().getCacheSize());
         readAheadCache.init();
      }
      tm = (TransactionManager) container.getTransactionManager();
   }

   public void start() throws Exception {
      super.start();
      JDBCFindEntitiesCommand find = (JDBCFindEntitiesCommand)findEntitiesCommand;
      find.start();
      if (readAheadCache != null) {
         readAheadCache.start();
      }
   }

   public void stop() {
      super.stop();
      if (readAheadCache != null) {
         readAheadCache.stop();
      }
   }

   public void destroy() {
      super.destroy();
      if (readAheadCache != null) {
         readAheadCache.stop();
         readAheadCache = null;
      }
   }

   public JDBCEntityBridge getEntityBridge() {
      return entityBridge;
   }

   public JDBCTypeFactory getJDBCTypeFactory() {
      return typeFactory;
   }

   public boolean getDebug() {
      return debug;
   }

   public JDBCEntityMetaData getMetaData() {
      return metaData;
   }

   protected CommandFactory createCommandFactory() throws Exception {
      return new JDBCCommandFactory(this);
   }

   public JDBCCommandFactory getCommandFactory() {
      return (JDBCCommandFactory) commandFactory;
   }

   public void loadEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      boolean done = false;
      JDBCCMPFieldBridge[] fieldsToLoad;

      // is any on the data already in the entity valid
      if(!ctx.isValid()) {
         entityBridge.resetPersistenceContext(ctx);
      }

      if (readAheadOnLoad) {
         fieldsToLoad = entityBridge.getEagerLoadFields();
         if ((fieldsToLoad.length == 0) || readAheadFields(fieldsToLoad, ctx)) {
            done = true;
         }
      }
      if (!done) {
         super.loadEntity(ctx);
      }
   }

   public void loadField(JDBCCMPFieldBridge field, EntityEnterpriseContext ctx) {
      boolean done = false;
      JDBCCMPFieldBridge[] fieldsToLoad;

      fieldsToLoad = loadFieldCommand.getFieldGroupsUnion(field);
      if (readAheadOnLoad) {
         if (readAheadFields(fieldsToLoad, ctx)) {
            done = true;
         }
      }
      if (!done) {
         loadFieldCommand.execute(fieldsToLoad, ctx);
      }
   }

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
            } catch (RemoteException ex) {
               log.warn("Read ahead failed", ex);
            }
         }
      }
      return success;
   }

    /**
   * Returns a new instance of a class which implemnts the bean class.
   *
   * @see java.lang.Class#newInstance
   * @return the new instance
   */
   public Object createBeanClassInstance() throws Exception {
      Class beanClass = container.getBeanClass();

      Class[] classes = new Class[] { beanClass };
      EntityBridgeInvocationHandler handler = new EntityBridgeInvocationHandler(container, entityBridge, beanClass);
      ClassLoader classLoader = beanClass.getClassLoader();

      return Proxy.newProxyInstance(classLoader, classes, handler);
   }

   /**
    * Returns a database connection
    */
   public Connection getConnection() throws SQLException {
      return dataSource.getConnection();
   }

   public Set findByForeignKey(Object foreignKey, JDBCCMPFieldBridge[] foreignKeyFields) {
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
      ApplicationMetaData amd = container.getBeanMetaData().getApplicationMetaData();

      // Get Tx Hashtable
      return (Map)amd.getPluginData("CMP-JDBC-TX-DATA");
   }

   private void initTxDataMap() {
      ApplicationMetaData amd = container.getBeanMetaData().getApplicationMetaData();

      // Get Tx Hashtable
      Map txDataMap = (Map)amd.getPluginData("CMP-JDBC-TX-DATA");
      if(txDataMap == null) {
         // we are the first JDBC CMP manager to get to initTxDataMap.
         txDataMap = Collections.synchronizedMap(new HashMap());
         amd.addPluginData("CMP-JDBC-TX-DATA", txDataMap);
      }
   }

   private JDBCEntityMetaData loadJDBCEntityMetaData() throws DeploymentException {
      ApplicationMetaData amd = container.getBeanMetaData().getApplicationMetaData();

      // Get JDBC MetaData
      JDBCApplicationMetaData jamd = (JDBCApplicationMetaData)amd.getPluginData("CMP-JDBC");
      if (jamd == null) {
         // we are the first cmp entity to need jbosscmp-jdbc. Load jbosscmp-jdbc.xml for the whole application
         JDBCXmlFileLoader jfl = new JDBCXmlFileLoader(amd, container.getClassLoader(), container.getLocalClassLoader(), log);
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
   void addPreloadData(Object entityKey, JDBCCMPFieldBridge field, Object fieldValue)
   {
      Transaction trans = null;
      PreloadKey preloadKey;

      try {
         trans = tm.getTransaction();
      } catch (javax.transaction.SystemException sysE) {
         log.warn("System exception getting transaction for preload - can't preload data for "+entityKey, sysE);
         return;
      }

      if (trans != null && !transactions.contains(trans)) {
         synchronized (transactions) { // synchronize only if absolutely necessary
            if (!transactions.contains(trans)) {
               try {
                  trans.registerSynchronization(new PreloadClearSynch(trans));
               } catch (javax.transaction.SystemException se) {
                  log.warn("System exception getting transaction for preload - can't get preloaded data for "+entityKey, se);
                  return;
               } catch (javax.transaction.RollbackException re) {
                  log.warn("Rollback exception getting transaction for preload - can't get preloaded data for "+entityKey, re);
                  return;
               }
               transactions.add(trans);
            }
         }
      }
      preloadKey = new PreloadKey(trans, entityKey, field.getMetaData().getFieldName());
      preloadedData.put(preloadKey, (fieldValue == null ? NULL_VALUE : fieldValue));
   }

   /**
    * Get data that we might have preloaded for an entity in a transaction
    * @param fieldValueRef will be filled with the field value
    * @return whether the data was found in the pool (null field value doesn't mean that it wasn't).
    */
   private boolean getPreloadData(Object entityKey, JDBCCMPFieldBridge field, Object[] fieldValueRef)
   {
      Transaction trans = null;
      PreloadKey preloadKey;
      Object fieldValue;
      boolean found;

      try {
         trans = tm.getTransaction();
      } catch (javax.transaction.SystemException sysE) {
         log.warn("System exception getting transaction for preload - not preloading "+entityKey, sysE);
         return false;
      }

      preloadKey = new PreloadKey(trans, entityKey, field.getMetaData().getFieldName());
      fieldValue = preloadedData.remove(preloadKey);
      log.debug("Getting Preload " + preloadKey + " " + field.getMetaData().getFieldName() + " " + fieldValue);
      found = (fieldValue != null);
      if (fieldValue == NULL_VALUE) { // due to this trick we avoid synchronization on preloadedData
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

      if (transactions.remove(trans)) {
         for (Iterator it = preloadedData.entrySet().iterator(); it.hasNext(); ) {
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

         return ((trans == preloadKey.trans) && field.equals(preloadKey.field) &&
                 key.equals(preloadKey.key));
      }

      public int hashCode() {
         return (key.hashCode() + field.hashCode() + (trans == null ? 0 : trans.hashCode()));
      }
   }

   private class PreloadClearSynch implements javax.transaction.Synchronization {
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
