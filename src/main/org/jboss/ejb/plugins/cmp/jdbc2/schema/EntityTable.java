/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCEntityPersistenceStore;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCTypeFactory;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMPFieldBridge2;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ejb.DuplicateKeyException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.EJBException;
import javax.transaction.Transaction;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;


/**
 * todo refactor optimistic locking
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.8 $</tt>
 */
public class EntityTable
   implements Table
{
   private static final byte UNREFERENCED = 0;
   private static final byte CLEAN = 1;
   private static final byte DIRTY = 2;
   private static final byte CREATED = 4;
   private static final byte DELETED = 8;

   private JDBCEntityBridge2 entity;
   private String tableName;
   private int fieldsTotal;
   private int relationsTotal;
   private DataSource dataSource;
   private Schema schema;
   private int tableId;
   private boolean dontFlushCreated;

   private String deleteSql;
   private String updateSql;
   private String insertSql;
   private String selectSql;
   private String duplicatePkSql;

   private final CommitStrategy insertStrategy;
   private final CommitStrategy deleteStrategy;
   private final CommitStrategy updateStrategy;

   private Logger log;

   private Cache cache;
   private ServiceControllerMBean serviceController;
   private ObjectName cacheName;

   public EntityTable(JDBCEntityMetaData metadata, JDBCEntityBridge2 entity, Schema schema, int tableId)
      throws DeploymentException
   {
      try
      {
         InitialContext ic = new InitialContext();
         dataSource = (DataSource) ic.lookup(metadata.getDataSourceName());
      }
      catch(NamingException e)
      {
         throw new DeploymentException("Filed to lookup: " + metadata.getDataSourceName(), e);
      }

      this.entity = entity;
      tableName = SQLUtil.fixTableName(metadata.getDefaultTableName(), dataSource);
      log = Logger.getLogger(getClass().getName() + "." + tableName);

      this.schema = schema;
      this.tableId = tableId;

      final ConfigurationMetaData containerConf = entity.getContainer().getBeanMetaData().getContainerConfiguration();
      dontFlushCreated = containerConf.isInsertAfterEjbPostCreate();

      // create cache
      final Element cacheConf = containerConf.getContainerCacheConf();
      final Element batchCommitStrategy;
      if(cacheConf == null)
      {
         cache = new PartitionedTableCache(500, 1000, 10);
         batchCommitStrategy = null;
      }
      else
      {
         cache = new PartitionedTableCache(cacheConf);
         batchCommitStrategy = MetaData.getOptionalChild(cacheConf, "batch-commit-strategy");
      }

      if(batchCommitStrategy == null)
      {
         insertStrategy = NON_BATCH_UPDATE;
         deleteStrategy = NON_BATCH_UPDATE;
         updateStrategy = NON_BATCH_UPDATE;
      }
      else
      {
         log.debug("batch-commit-strategy");
         insertStrategy = BATCH_UPDATE;
         deleteStrategy = BATCH_UPDATE;
         updateStrategy = BATCH_UPDATE;
      }

      final MBeanServer server = MBeanServerLocator.locateJBoss();
      serviceController = (ServiceControllerMBean)
         MBeanProxyExt.create(ServiceControllerMBean.class,
            ServiceControllerMBean.OBJECT_NAME,
            server);
      try
      {
         cacheName =
            new ObjectName("jboss.cmp:service=tablecache,ejbname=" + metadata.getName() + ",table=" + tableName);
         server.registerMBean(cache, cacheName);
         serviceController.create(cacheName);
      }
      catch(Exception e)
      {
         throw new DeploymentException("Failed to register table cache for " + tableName, e);
      }
   }

   public void start() throws DeploymentException
   {
      final JDBCAbstractCMRFieldBridge[] cmrFields = entity.getCMRFields();
      relationsTotal = (cmrFields != null ? cmrFields.length : 0);

      JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();
      JDBCCMPFieldBridge2[] tableFields = (JDBCCMPFieldBridge2[]) entity.getTableFields();

      // DELETE SQL
      deleteSql = "delete from " + tableName + " where ";
      deleteSql += pkFields[0].getColumnName() + "=?";
      for(int i = 1; i < pkFields.length; ++i)
      {
         deleteSql += " and " + pkFields[i].getColumnName() + "=?";
      }
      log.debug("delete sql: " + deleteSql);

      // INSERT SQL
      insertSql = "insert into " + tableName + "(";
      insertSql += tableFields[0].getColumnName();
      for(int i = 1; i < tableFields.length; ++i)
      {
         insertSql += ", " + tableFields[i].getColumnName();
      }
      insertSql += ") values (?";
      for(int i = 1; i < tableFields.length; ++i)
      {
         insertSql += ", ?";
      }
      insertSql += ")";
      log.debug("insert sql: " + insertSql);

      // UPDATE SQL
      updateSql = "update " + tableName + " set ";
      int setFields = 0;
      for(int i = 0; i < tableFields.length; ++i)
      {
         JDBCCMPFieldBridge2 field = tableFields[i];
         if(!field.isPrimaryKeyMember())
         {
            if(setFields++ > 0)
            {
               updateSql += ", ";
            }
            updateSql += field.getColumnName() + "=?";
         }
      }
      updateSql += " where ";
      updateSql += pkFields[0].getColumnName() + "=?";
      for(int i = 1; i < pkFields.length; ++i)
      {
         updateSql += " and " + pkFields[i].getColumnName() + "=?";
      }

      if(entity.getVersionField() != null)
      {
         updateSql += " and " + entity.getVersionField().getColumnName() + "=?";
      }
      log.debug("update sql: " + updateSql);

      // SELECT SQL
      String selectColumns = tableFields[0].getColumnName();
      for(int i = 1; i < tableFields.length; ++i)
      {
         JDBCCMPFieldBridge2 field = tableFields[i];
         selectColumns += ", " + field.getColumnName();
      }

      String whereColumns = pkFields[0].getColumnName() + "=?";
      for(int i = 1; i < pkFields.length; ++i)
      {
         whereColumns += " and " + pkFields[i].getColumnName() + "=?";
      }

      if(entity.getMetaData().hasRowLocking())
      {
         JDBCEntityPersistenceStore manager = entity.getManager();
         JDBCTypeFactory typeFactory = manager.getJDBCTypeFactory();
         JDBCTypeMappingMetaData typeMapping = typeFactory.getTypeMapping();
         JDBCFunctionMappingMetaData rowLockingTemplate = typeMapping.getRowLockingTemplate();
         if(rowLockingTemplate == null)
         {
            throw new DeploymentException("Row locking template is not defined for mapping: " + typeMapping.getName());
         }

         selectSql = rowLockingTemplate.getFunctionSql(new Object[]{selectColumns, tableName, whereColumns, null},
            new StringBuffer()).toString();
      }
      else
      {
         selectSql = "select ";
         selectSql += selectColumns;
         selectSql += " from " + tableName + " where ";
         selectSql += whereColumns;
      }
      log.debug("select sql: " + selectSql);

      // DUPLICATE KEY
      if(dontFlushCreated)
      {
         duplicatePkSql = "select ";
         duplicatePkSql += pkFields[0].getColumnName();
         for(int i = 1; i < pkFields.length; ++i)
         {
            duplicatePkSql += ", " + pkFields[i].getColumnName();
         }
         duplicatePkSql += " from " + tableName + " where ";
         duplicatePkSql += pkFields[0].getColumnName() + "=?";
         for(int i = 1; i < pkFields.length; ++i)
         {
            duplicatePkSql += " and " + pkFields[i].getColumnName() + "=?";
         }
         log.debug("duplicate pk sql: " + duplicatePkSql);
      }

      try
      {
         cache.start();
      }
      catch(Exception e)
      {
         throw new DeploymentException("Failed to start cache", e);
      }

      try
      {
         serviceController.start(cacheName);
      }
      catch(Exception e)
      {
         throw new DeploymentException("Failed to start table cache.", e);
      }
   }

   public void stop() throws Exception
   {
      serviceController.stop(cacheName);
      serviceController.destroy(cacheName);
      serviceController.remove(cacheName);
      serviceController = null;
   }

   public StringBuffer appendColumnNames(JDBCCMPFieldBridge2[] fields, String alias, StringBuffer buf)
   {
      for(int i = 0; i < fields.length; ++i)
      {
         if(i > 0)
         {
            buf.append(", ");
         }

         if(alias != null)
         {
            buf.append(alias).append(".");
         }

         buf.append(fields[i].getColumnName());
      }

      return buf;
   }

   public void addField()
   {
      ++fieldsTotal;
   }

   public int addVersionField()
   {
      return fieldsTotal++;
   }

   public DataSource getDataSource()
   {
      return dataSource;
   }

   public void loadRow(ResultSet rs, Object pk)
   {
      View view = getView();
      view.loadRow(rs, pk);
   }

   public Object loadRow(ResultSet rs)
   {
      Row row = null;
      View view = getView();
      Object pk = view.loadPk(rs);
      if(pk != null)
      {
         row = view.loadRow(rs, pk);
      }
      else if(log.isTraceEnabled())
      {
         log.trace("loaded pk is null.");
      }
      return row.pk;
   }

   public Row getRow(Object id)
   {
      return getView().getRow(id);
   }

   public boolean hasRow(Object id)
   {
      return getView().hasRow(id);
   }

   public Row loadRow(Object id) throws SQLException, ObjectNotFoundException
   {
      View view = getView();

      Row row = view.getRowByPk(id, false);
      if(row != null)
      {
         if(log.isTraceEnabled())
         {
            log.trace("row is already loaded: pk=" + id);
         }
         return row;
      }

      JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();

      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
         if(log.isDebugEnabled())
         {
            log.debug("executing sql: " + selectSql);
         }

         con = dataSource.getConnection();
         ps = con.prepareStatement(selectSql);

         int paramInd = 1;
         for(int i = 0; i < pkFields.length; ++i)
         {
            JDBCCMPFieldBridge2 pkField = pkFields[i];
            Object pkValue = pkField.getPrimaryKeyValue(id);
            paramInd = pkField.setArgumentParameters(ps, paramInd, pkValue);
         }

         rs = ps.executeQuery();

         if(!rs.next())
         {
            throw new ObjectNotFoundException("Row not found: " + id);
         }

         return view.loadRow(rs, id);
      }
      catch(SQLException e)
      {
         log.error("Failed to load row: table=" + tableName + ", pk=" + id);
         throw e;
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   // Table implementation

   public int getTableId()
   {
      return tableId;
   }

   public String getTableName()
   {
      return tableName;
   }

   public Table.View createView(Transaction tx)
   {
      return new View(tx);
   }

   // Private

   private void delete(View view) throws SQLException
   {
      if(view.deleted == null)
      {
         if(log.isTraceEnabled())
         {
            log.trace("no rows to delete");
         }
         return;
      }

      JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();

      Connection con = null;
      PreparedStatement ps = null;
      try
      {
         if(log.isDebugEnabled())
         {
            log.debug("executing : " + deleteSql);
         }

         con = dataSource.getConnection();
         ps = con.prepareStatement(deleteSql);

         int batchCount = 0;
         while(view.deleted != null)
         {
            Row row = view.deleted;

            int paramInd = 1;
            for(int pkInd = 0; pkInd < pkFields.length; ++pkInd)
            {
               JDBCCMPFieldBridge2 pkField = pkFields[pkInd];
               Object fieldValue = row.getFieldValue(pkField.getRowIndex());
               paramInd = pkField.setArgumentParameters(ps, paramInd, fieldValue);
            }

            deleteStrategy.executeUpdate(ps);

            ++batchCount;
            row.flushStatus();
         }

         deleteStrategy.executeBatch(ps);

         if(view.deleted != null)
         {
            throw new IllegalStateException("There are still rows to delete!");
         }

         if(log.isTraceEnabled())
         {
            log.trace("deleted rows: " + batchCount);
         }
      }
      catch(SQLException e)
      {
         log.error("Failed to delete view: " + e.getMessage(), e);
         throw e;
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   private void update(View view) throws SQLException
   {
      if(view.dirty == null)
      {
         if(log.isTraceEnabled())
         {
            log.trace("no rows to update");
         }
         return;
      }

      JDBCCMPFieldBridge2[] tableFields = (JDBCCMPFieldBridge2[]) entity.getTableFields();
      JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();

      Connection con = null;
      PreparedStatement ps = null;
      try
      {
         if(log.isDebugEnabled())
         {
            log.debug("executing : " + updateSql);
         }

         con = dataSource.getConnection();
         ps = con.prepareStatement(updateSql);

         int batchCount = 0;
         while(view.dirty != null)
         {
            Row row = view.dirty;

            int paramInd = 1;
            for(int fInd = 0; fInd < tableFields.length; ++fInd)
            {
               JDBCCMPFieldBridge2 field = tableFields[fInd];
               if(!field.isPrimaryKeyMember())
               {
                  Object fieldValue = row.getFieldValue(field.getRowIndex());
                  paramInd = field.setArgumentParameters(ps, paramInd, fieldValue);
               }
            }

            for(int fInd = 0; fInd < pkFields.length; ++fInd)
            {
               JDBCCMPFieldBridge2 pkField = pkFields[fInd];
               Object fieldValue = row.getFieldValue(pkField.getRowIndex());
               paramInd = pkField.setArgumentParameters(ps, paramInd, fieldValue);
            }

            JDBCCMPFieldBridge2 versionField = entity.getVersionField();
            if(versionField != null)
            {
               int versionIndex = versionField.getVersionIndex();
               Object curVersion = row.getFieldValue(versionIndex);
               paramInd = versionField.setArgumentParameters(ps, paramInd, curVersion);

               Object newVersion = row.getFieldValue(versionField.getRowIndex());
               row.setFieldValue(versionIndex, newVersion);
            }

            updateStrategy.executeUpdate(ps);

            ++batchCount;
            row.flushStatus();
         }

         updateStrategy.executeBatch(ps);

         if(log.isTraceEnabled())
         {
            log.trace("updated rows: " + batchCount);
         }
      }
      catch(SQLException e)
      {
         log.error("Failed to update: table=" + tableName, e);
         throw e;
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   private void insert(View view) throws SQLException
   {
      if(dontFlushCreated || view.created == null)
      {
         if(log.isTraceEnabled())
         {
            log.trace("no rows to insert");
         }
         return;
      }

      JDBCCMPFieldBridge2[] tableFields = (JDBCCMPFieldBridge2[]) entity.getTableFields();
      Connection con = null;
      PreparedStatement ps = null;
      try
      {
         if(log.isDebugEnabled())
         {
            log.debug("executing : " + insertSql);
         }

         con = dataSource.getConnection();
         ps = con.prepareStatement(insertSql);

         int batchCount = 0;
         while(view.created != null)
         {
            Row row = view.created;

            int paramInd = 1;
            for(int fInd = 0; fInd < tableFields.length; ++fInd)
            {
               JDBCCMPFieldBridge2 field = tableFields[fInd];
               Object fieldValue = row.getFieldValue(field.getRowIndex());
               paramInd = field.setArgumentParameters(ps, paramInd, fieldValue);
            }

            insertStrategy.executeUpdate(ps);

            ++batchCount;
            row.flushStatus();
         }

         insertStrategy.executeBatch(ps);

         if(log.isTraceEnabled())
         {
            log.trace("inserted rows: " + batchCount);
         }
      }
      catch(SQLException e)
      {
         log.error("Failed to insert new rows: " + e.getMessage(), e);
         throw e;
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   private EntityTable.View getView()
   {
      return (EntityTable.View) schema.getView(this);
   }

   public class View implements Table.View
   {
      private final Transaction tx;

      private Map rowByPk = new HashMap();
      private Row created;
      private Row deleted;
      private Row dirty;
      private Row clean;

      private Row cacheUpdates;

      public View(Transaction tx)
      {
         this.tx = tx;
      }

      public Row getRow(Object pk)
      {
         Row row;
         if(pk == null)
         {
            row = new Row(this);
         }
         else
         {
            row = getRowByPk(pk, false);
            if(row == null)
            {
               row = createCleanRow(pk);
            }
         }
         return row;
      }

      public Row getRowByPk(Object pk, boolean required)
      {
         /*
         Row cursor = clean;
         while(cursor != null)
         {
            if(pk.equals(cursor.pk))
            {
               return cursor;
            }
            cursor = cursor.next;
         }

         cursor = dirty;
         while(cursor != null)
         {
            if(pk.equals(cursor.pk))
            {
               return cursor;
            }
            cursor = cursor.next;
         }

         cursor = created;
         while(cursor != null)
         {
            if(pk.equals(cursor.pk))
            {
               return cursor;
            }
            cursor = cursor.next;
         }
         */

         Row row = (Row) rowByPk.get(pk);

         if(row == null)
         {
            Object[] fields;
            Object[] relations = null;
            try
            {
               cache.lock(pk);

               fields = cache.getFields(pk);
               if(fields != null && relationsTotal > 0)
               {
                  relations = cache.getRelations(pk);
                  if(relations == null)
                  {
                     relations = new Object[relationsTotal];
                  }
               }
            }
            finally
            {
               cache.unlock(pk);
            }

            if(fields != null)
            {
               row = createCleanRow(pk, fields, relations);
            }
         }

         if(row == null && required)
         {
            throw new IllegalStateException("row not found: pk=" + pk);
         }

         return row;
      }

      public void addClean(Row row)
      {
         /*
         if(getRowByPk(row.pk, false) != null)
         {
            throw new IllegalStateException("View already contains the row: key=" + row.pk);
         }
         */

         if(clean != null)
         {
            row.next = clean;
            clean.prev = row;
         }

         clean = row;
         row.state = CLEAN;

         rowByPk.put(row.pk, row);
      }

      public void addCreated(Row row) throws DuplicateKeyException
      {
         //if(getRowByPk(row.pk, false) != null)
         //{
         //   throw new DuplicateKeyException("Table " + tableName + ", key=" + row.pk);
         //}

         if(created != null)
         {
            row.next = created;
            created.prev = row;
         }

         created = row;
         row.state = CREATED;

         rowByPk.put(row.pk, row);

         JDBCCMPFieldBridge2 versionField = entity.getVersionField();
         if(versionField != null)
         {
            row.fields[versionField.getVersionIndex()] = row.fields[versionField.getRowIndex()];
         }
      }

      public Row loadRow(ResultSet rs, Object pk)
      {
         Row row = getRowByPk(pk, false);
         if(row != null)
         {
            if(log.isTraceEnabled())
            {
               log.trace("row is already loaded: pk=" + pk);
            }
            return row;
         }
         else if(log.isTraceEnabled())
         {
            log.trace("reading result set: pk=" + pk);
         }

         row = createCleanRow(pk);
         JDBCCMPFieldBridge2[] tableFields = (JDBCCMPFieldBridge2[]) entity.getTableFields();
         //int rsInd = 1;
         for(int i = 0; i < tableFields.length; ++i)
         {
            JDBCCMPFieldBridge2 field = tableFields[i];
            //Object columnValue = field.loadArgumentResults(rs, rsInd++);
            Object columnValue = field.loadArgumentResults(rs, field.getRowIndex() + 1);
            row.fields[field.getRowIndex()] = columnValue;

            if(field.getVersionIndex() != -1)
            {
               row.fields[field.getVersionIndex()] = columnValue;
            }
         }

         Object[] relations = (relationsTotal > 0 ? new Object[relationsTotal] : null);

         try
         {
            cache.lock(row.pk);
            cache.put(tx, row.pk, row.fields, relations);
         }
         finally
         {
            cache.unlock(row.pk);
         }

         return row;
      }

      public Object loadPk(ResultSet rs)
      {
         Object pk = null;
         JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();
         //int rsInd = 1;
         for(int i = 0; i < pkFields.length; ++i)
         {
            JDBCCMPFieldBridge2 field = pkFields[i];
            //Object columnValue = field.loadArgumentResults(rs, rsInd++);
            Object columnValue = field.loadArgumentResults(rs, field.getRowIndex() + 1);
            pk = field.setPrimaryKeyValue(pk, columnValue);
         }
         return pk;
      }

      public boolean hasRow(Object id)
      {
         boolean has = rowByPk.containsKey(id);
         if(!has)
         {
            try
            {
               cache.lock(id);
               has = cache.contains(tx, id);
            }
            finally
            {
               cache.unlock(id);
            }
         }
         return has;
      }

      private Row createCleanRow(Object pk)
      {
         Row row = new Row(this);
         row.pk = pk;
         addClean(row);
         return row;
      }

      private Row createCleanRow(Object pk, Object[] fields, Object[] relations)
      {
         Row row = new Row(this, fields, relations);
         row.pk = pk;
         addClean(row);
         return row;
      }

      // Table.View implementation

      public void flush() throws SQLException
      {
         delete(this);
         update(this);
         insert(this);
      }

      public void beforeCompletion()
      {
         if(cacheUpdates != null)
         {
            Row cursor = cacheUpdates;

            while(cursor != null)
            {
               cache.lock(cursor.pk);
               try
               {
                  cache.lockForUpdate(tx, cursor.pk);
               }
               catch(Exception e)
               {
                  throw new EJBException("Table " + entity.getTableName() + ": " + e.getMessage());
               }
               finally
               {
                  cache.unlock(cursor.pk);
               }

               cursor.lockedForUpdate = true;
               cursor = cursor.nextCacheUpdate;
            }
         }
      }

      public void committed()
      {
         if(cacheUpdates != null)
         {
            Row cursor = cacheUpdates;

            while(cursor != null)
            {
               if(cursor.lockedForUpdate)
               {
                  cache.lock(cursor.pk);
                  try
                  {
                     switch(cursor.state)
                     {
                        case CLEAN:
                           cache.put(tx, cursor.pk, cursor.fields, cursor.relations);
                           break;
                        case DELETED:
                           try
                           {
                              cache.remove(tx, cursor.pk);
                           }
                           catch(Exception e)
                           {
                              log.warn(e.getMessage());
                           }
                           break;
                        default:
                           throw new IllegalStateException("Unexpected row state: table=" +
                              entity.getTableName() +
                              ", pk=" + cursor.pk + ", state=" + cursor.state);
                     }
                  }
                  finally
                  {
                     cache.unlock(cursor.pk);
                  }
                  cursor.lockedForUpdate = false;
               }
               cursor = cursor.nextCacheUpdate;
            }
         }
      }

      public void rolledback()
      {
         if(cacheUpdates != null)
         {
            Row cursor = cacheUpdates;

            while(cursor != null)
            {
               if(cursor.lockedForUpdate)
               {
                  cache.lock(cursor.pk);
                  try
                  {
                     cache.releaseLock(tx, cursor.pk);
                  }
                  catch(Exception e)
                  {
                     log.warn("Table " + entity.getTableName() + ": " + e.getMessage());
                  }
                  finally
                  {
                     cache.unlock(cursor.pk);
                  }
                  cursor.lockedForUpdate = false;
               }
               cursor = cursor.nextCacheUpdate;
            }
         }
      }
   }

   public class Row
   {
      private EntityTable.View view;
      private Object pk;
      private final Object[] fields;
      private final Object[] relations;

      private byte state;

      private Row prev;
      private Row next;

      private boolean cacheUpdateScheduled;
      private Row nextCacheUpdate;
      private boolean lockedForUpdate;

      public Row(EntityTable.View view)
      {
         this.view = view;
         fields = new Object[fieldsTotal];
         relations = (relationsTotal == 0 ? null : new Object[relationsTotal]);
         state = UNREFERENCED;
      }

      public Row(EntityTable.View view, Object[] fields, Object[] relations)
      {
         this.view = view;
         this.fields = fields;
         this.relations = relations;
         state = UNREFERENCED;
      }

      public Object getPk()
      {
         return pk;
      }

      public void loadCachedRelations(int index, Cache.CacheLoader loader)
      {
         if(relations != null)
         {
            final Object cached = relations[index];
            relations[index] = loader.loadFromCache(cached);
         }
      }

      public void cacheRelations(int index, Cache.CacheLoader loader)
      {
         relations[index] = loader.getCachedValue();
         scheduleCacheUpdate();
      }

      public void insert(Object pk) throws DuplicateKeyException
      {
         this.pk = pk;
         view.addCreated(this);
      }

      public Object getFieldValue(int i)
      {
         return fields[i];
      }

      public void setFieldValue(int i, Object value)
      {
         fields[i] = value;
      }

      public boolean isDirty()
      {
         return state != CLEAN;
      }

      public void setDirty()
      {
         if(state == CLEAN)
         {
            updateState(DIRTY);
         }
      }

      public void delete()
      {
         if(state == CLEAN || state == DIRTY)
         {
            updateState(DELETED);
         }
         else if(state == CREATED)
         {
            dereference();
            state = DELETED;
            view.rowByPk.remove(pk);
         }
         else if(state == DELETED)
         {
            throw new IllegalStateException("The row is already deleted: pk=" + pk);
         }
      }

      private void flushStatus()
      {
         if(state == CREATED || state == DIRTY)
         {
            updateState(CLEAN);
         }
         else if(state == DELETED)
         {
            dereference();
         }

         scheduleCacheUpdate();
      }

      private void scheduleCacheUpdate()
      {
         if(!cacheUpdateScheduled)
         {
            if(view.cacheUpdates == null)
            {
               view.cacheUpdates = this;
            }
            else
            {
               nextCacheUpdate = view.cacheUpdates;
               view.cacheUpdates = this;
            }
            cacheUpdateScheduled = true;
         }
      }

      private void updateState(byte state)
      {
         dereference();

         if(state == CLEAN)
         {
            if(view.clean != null)
            {
               next = view.clean;
               view.clean.prev = this;
            }
            view.clean = this;
         }
         else if(state == DIRTY)
         {
            if(view.dirty != null)
            {
               next = view.dirty;
               view.dirty.prev = this;
            }
            view.dirty = this;
         }
         else if(state == CREATED)
         {
            if(view.created != null)
            {
               next = view.created;
               view.created.prev = this;
            }
            view.created = this;
         }
         else if(state == DELETED)
         {
            if(view.deleted != null)
            {
               next = view.deleted;
               view.deleted.prev = this;
            }
            view.deleted = this;
         }
         else
         {
            throw new IllegalStateException("Can't update to state: " + state);
         }

         this.state = state;
      }

      private void dereference()
      {
         if(state == CLEAN && view.clean == this)
         {
            view.clean = next;
         }
         else if(state == DIRTY && view.dirty == this)
         {
            view.dirty = next;
         }
         else if(state == CREATED && view.created == this)
         {
            view.created = next;
         }
         else if(state == DELETED && view.deleted == this)
         {
            view.deleted = next;
         }

         if(next != null)
         {
            next.prev = prev;
         }

         if(prev != null)
         {
            prev.next = next;
         }

         prev = null;
         next = null;
      }

      public void flush() throws SQLException, DuplicateKeyException
      {
         // todo needs refactoring

         if(state != CREATED)
         {
            if(log.isTraceEnabled())
            {
               log.trace("The row is already inserted: pk=" + pk);
            }
            return;
         }

         Connection con = null;
         PreparedStatement duplicatePkPs = null;
         PreparedStatement insertPs = null;
         ResultSet rs = null;
         try
         {
            int paramInd;
            con = dataSource.getConnection();

            // check for duplicate key
            /*
            if(log.isDebugEnabled())
            {
               log.debug("executing : " + duplicatePkSql);
            }

            duplicatePkPs = con.prepareStatement(duplicatePkSql);

            paramInd = 1;
            JDBCCMPFieldBridge2[] pkFields = (JDBCCMPFieldBridge2[]) entity.getPrimaryKeyFields();
            for(int i = 0; i < pkFields.length; ++i)
            {
               JDBCCMPFieldBridge2 pkField = pkFields[i];
               Object fieldValue = fields[pkField.getRowIndex()];
               paramInd = pkField.setArgumentParameters(duplicatePkPs, paramInd, fieldValue);
            }

            rs = duplicatePkPs.executeQuery();
            if(rs.next())
            {
               throw new DuplicateKeyException("Table " + tableName + ", pk=" + pk);
            }
            */

            // insert
            if(log.isDebugEnabled())
            {
               log.debug("executing : " + insertSql);
            }

            insertPs = con.prepareStatement(insertSql);

            paramInd = 1;
            JDBCCMPFieldBridge2[] tableFields = (JDBCCMPFieldBridge2[]) entity.getTableFields();
            for(int fInd = 0; fInd < tableFields.length; ++fInd)
            {
               JDBCCMPFieldBridge2 field = tableFields[fInd];
               Object fieldValue = fields[field.getRowIndex()];
               paramInd = field.setArgumentParameters(insertPs, paramInd, fieldValue);
            }

            insertPs.executeUpdate();

            flushStatus();
         }
         catch(SQLException e)
         {
            log.error("Failed to insert new rows: " + e.getMessage(), e);
            throw e;
         }
         finally
         {
            JDBCUtil.safeClose(rs);
            JDBCUtil.safeClose(duplicatePkPs);
            JDBCUtil.safeClose(insertPs);
            JDBCUtil.safeClose(con);
         }
      }
   }

   public static interface CommitStrategy
   {
      void executeUpdate(PreparedStatement ps) throws SQLException;

      void executeBatch(PreparedStatement ps) throws SQLException;
   }

   private static final CommitStrategy BATCH_UPDATE = new CommitStrategy()
   {
      public void executeUpdate(PreparedStatement ps) throws SQLException
      {
         ps.addBatch();
      }

      public void executeBatch(PreparedStatement ps) throws SQLException
      {
         int[] updates = ps.executeBatch();
         for(int i = 0; i < updates.length; ++i)
         {
            int status = updates[i];
            if(status != 1 && status != -2 /* java.sql.Statement.SUCCESS_NO_INFO since jdk1.4*/)
            {
               String msg = (status == -3 /* java.sql.Statement.EXECUTE_FAILED since jdk1.4 */ ?
                  "One of the commands in the batch failed to execute" :
                  "Each command in the batch should update exactly 1 row but " +
                  "one of the commands updated " + updates[i] + " rows.");
               throw new EJBException(msg);
            }
         }
      }
   };

   private static final CommitStrategy NON_BATCH_UPDATE = new CommitStrategy()
   {
      public void executeUpdate(PreparedStatement ps) throws SQLException
      {
         int rows = ps.executeUpdate();
         if(rows != 1)
         {
            throw new EJBException("Expected one updated row but got: " + rows);
         }
      }

      public void executeBatch(PreparedStatement ps)
      {
      }
   };
}
