/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationshipRoleMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;
import org.jboss.ejb.plugins.cmp.bridge.EntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCStartCommand creates the table if specified in xml.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @author <a href="mailto:heiko.rupp@cellent.de">Heiko W.Rupp</a>
 * @author <a href="mailto:joachim@cabsoft.be">Joachim Van der Auwera</a>
 * @version $Revision: 1.49 $
 */
public final class JDBCStartCommand
{
   private static final String IDX_POSTFIX = "_idx";
   private static final String COULDNT_SUSPEND = "Could not suspend current transaction before ";
   private static final String COULDNT_REATTACH = "Could not reattach original transaction after ";
   private final static Object CREATED_TABLES_KEY = new Object();
   private final JDBCEntityPersistenceStore manager;
   private final JDBCAbstractEntityBridge entity;
   private final JDBCEntityMetaData entityMetaData;
   private final Logger log;
   private static int idxCount = 0;

   public JDBCStartCommand(JDBCEntityPersistenceStore manager)
   {
      this.manager = manager;
      entity = manager.getEntityBridge();
      entityMetaData = manager.getMetaData();

      // Create the Log
      log = Logger.getLogger(this.getClass().getName() +
         "." +
         manager.getMetaData().getName());

      // Create the created tables set
      Set tables = (Set) manager.getApplicationData(CREATED_TABLES_KEY);
      if(tables == null)
      {
         manager.putApplicationData(CREATED_TABLES_KEY, new HashSet());
      }
   }

   public void execute() throws DeploymentException
   {
      Set existedTables = getExistedTables(manager);

      boolean tableExisted = SQLUtil.tableExists(entity.getQualifiedTableName(), entity.getDataSource());
      if(tableExisted)
      {
         existedTables.add(entity.getEntityName());
      }

      if(tableExisted)
      {
         if(entityMetaData.getAlterTable())
         {
            SQLUtil.OldColumns oldColumns = SQLUtil.getOldColumns(entity.getQualifiedTableName(), entity.getDataSource());
            ArrayList oldNames = oldColumns.getColumnNames();
            ArrayList oldTypes = oldColumns.getTypeNames();
            ArrayList oldSizes = oldColumns.getColumnSizes();
            SQLUtil.OldIndexes oldIndexes = null;
            ArrayList newNames = new ArrayList();
            JDBCFieldBridge fields[] = entity.getTableFields();
            String tableName = entity.getQualifiedTableName();
            for(int i = 0; i < fields.length; i++)
            {
               JDBCFieldBridge field = fields[i];
               JDBCType jdbcType = field.getJDBCType();
               String[] columnNames = jdbcType.getColumnNames();
               String[] sqlTypes = jdbcType.getSQLTypes();
               boolean[] notNull = jdbcType.getNotNull();

               for(int j = 0; j < columnNames.length; j++)
               {
                  String name = columnNames[j];
                  String ucName = name.toUpperCase();

                  newNames.add( ucName );

                  int oldIndex = oldNames.indexOf( ucName );
                  if(oldIndex == -1)
                  {
                     // add new column
                     StringBuffer buf = new StringBuffer( sqlTypes[j] );
                     if( notNull[j] )
                     {
                        buf.append(SQLUtil.NOT).append(SQLUtil.NULL);
                     }
                     alterTable(entity.getDataSource(),
                           entityMetaData.getTypeMapping().getAddColumnTemplate(),
                           tableName, name, buf.toString());
                  }
                  else
                  {
                     // alter existing columns
                     // only CHAR and VARCHAR fields are altered, and only when they are longer then before
                     String type = (String) oldTypes.get(oldIndex);
                     if(type.equals("CHAR") || type.equals("VARCHAR"))
                     {
                        try
                        {
                           // get new length
                           String l = sqlTypes[j];
                           l = l.substring(l.indexOf('(') + 1, l.length() - 1);
                           Integer oldLength = (Integer) oldSizes.get(oldIndex);
                           if(Integer.parseInt(l) > oldLength.intValue())
                           {
                              alterTable(entity.getDataSource(),
                                    entityMetaData.getTypeMapping().getAlterColumnTemplate(),
                                    tableName, name, sqlTypes[j] );
                           }
                        }
                        catch(Exception e)
                        {
                           log.warn("EXCEPTION ALTER :" + e.toString());
                        }
                     }
                  }
               }

               // see if we have to add an index for the field
               JDBCCMPFieldMetaData fieldMD = entity.getMetaData().getCMPFieldByName(field.getFieldName());
               if(fieldMD.isIndexed())
               {
                  if(oldIndexes == null)
                  {
                     oldIndexes = SQLUtil.getOldIndexes(entity.getQualifiedTableName(), entity.getDataSource());
                     idxCount = oldIndexes.getIndexNames().size();
                  }
                  if(!hasIndex(oldIndexes, field))
                  {
                     createCMPIndex(entity.getDataSource(), field);
                  }

               }
            } // for  int i;

            // delete old columns
            Iterator it = oldNames.iterator();
            while(it.hasNext())
            {
               String name = (String) (it.next());
               if(!newNames.contains(name))
               {
                  alterTable(entity.getDataSource(),
                        entityMetaData.getTypeMapping().getDropColumnTemplate(),
                        tableName, name, "");
               }
            }

         }
      }

      // Create table if necessary
      Set createdTables = getCreatedTables(manager);

      if(entityMetaData.getCreateTable() && !createdTables.contains(entity.getEntityName()))
      {
         DataSource dataSource = entity.getDataSource();
         createTable(dataSource, entity.getQualifiedTableName(), getEntityCreateTableSQL(dataSource));

         // create indices only if table did not yet exist.
         if(!tableExisted)
         {
            createCMPIndices(dataSource);
         }
         else
         {
            if(log.isDebugEnabled())
            {
               log.debug("Indices for table " + entity.getQualifiedTableName() + "not created as table existed");
            }
         }


         // issue extra (user-defined) sql for table
         if(!tableExisted)
         {
            issuePostCreateSQL(dataSource,
               entity.getMetaData().getDefaultTablePostCreateCmd(),
               entity.getQualifiedTableName());
         }

         createdTables.add(entity.getEntityName());
      }
      else
      {
         log.debug("Table not create as requested: " + entity.getQualifiedTableName());
      }

      // create relation tables
      JDBCAbstractCMRFieldBridge[] cmrFields = entity.getCMRFields();
      for(int i = 0; i < cmrFields.length; ++i)
      {
         JDBCAbstractCMRFieldBridge cmrField = cmrFields[i];
         JDBCRelationMetaData relationMetaData = cmrField.getMetaData().getRelationMetaData();

         // if the table for the related entity has been created
         final EntityBridge relatedEntity = cmrField.getRelatedEntity();
         if(relationMetaData.isTableMappingStyle() && createdTables.contains(relatedEntity.getEntityName()))
         {
            DataSource dataSource = relationMetaData.getDataSource();

            boolean relTableExisted = SQLUtil.tableExists(cmrField.getQualifiedTableName(), entity.getDataSource());

            if(relTableExisted)
            {
               if(relationMetaData.getAlterTable())
               {
                  ArrayList oldNames = SQLUtil.getOldColumns(cmrField.getQualifiedTableName(), dataSource).getColumnNames();
                  ArrayList newNames = new ArrayList();
                  JDBCFieldBridge[] leftKeys = cmrField.getTableKeyFields();
                  JDBCFieldBridge[] rightKeys = cmrField.getRelatedCMRField().getTableKeyFields();
                  JDBCFieldBridge[] fields = new JDBCFieldBridge[leftKeys.length + rightKeys.length];
                  System.arraycopy(leftKeys, 0, fields, 0, leftKeys.length);
                  System.arraycopy(rightKeys, 0, fields, leftKeys.length, rightKeys.length);
                  // have to append field names to leftKeys, rightKeys...

                  boolean different = false;
                  for(int j = 0; j < fields.length; j++)
                  {
                     JDBCFieldBridge field = fields[j];

                     String name = field.getJDBCType().getColumnNames()[0].toUpperCase();
                     newNames.add(name);

                     if(!oldNames.contains(name))
                     {
                        different = true;
                        break;
                     }
                  } // for int j;

                  if(!different)
                  {
                     Iterator it = oldNames.iterator();
                     while(it.hasNext())
                     {
                        String name = (String) (it.next());
                        if(!newNames.contains(name))
                        {
                           different = true;
                           break;
                        }
                     }
                  }

                  if(different)
                  {
                     // only log, don't drop table is this can cause data loss
                     log.error("CMR table structure is incorrect for " + cmrField.getQualifiedTableName());
                     //SQLUtil.dropTable(entity.getDataSource(), cmrField.getQualifiedTableName());
                  }

               } // if alter-table

            } // if existed

            // create the relation table
            if(relationMetaData.isTableMappingStyle() && !relationMetaData.isTableCreated())
            {
               if(relationMetaData.getCreateTable())
               {
                  createTable(dataSource, cmrField.getQualifiedTableName(),
                     getRelationCreateTableSQL(cmrField, dataSource));
               }
               else
               {
                  log.debug("Relation table not created as requested: " + cmrField.getQualifiedTableName());
               }
               // create Indices if needed
               createCMRIndex(dataSource, cmrField);

               if(relationMetaData.getCreateTable())
               {
                  issuePostCreateSQL(dataSource,
                     ((JDBCAbstractEntityBridge) relatedEntity).getMetaData()
                     .getDefaultTablePostCreateCmd(),
                     cmrField.getQualifiedTableName());
               }
            }
         }
      }
   }

   public void addForeignKeyConstraints() throws DeploymentException
   {
      // Create table if necessary
      Set createdTables = getCreatedTables(manager);

      JDBCAbstractCMRFieldBridge[] cmrFields = entity.getCMRFields();
      for(int i = 0; i < cmrFields.length; ++i)
      {
         JDBCAbstractCMRFieldBridge cmrField = cmrFields[i];
         JDBCRelationMetaData relationMetaData = cmrField.getMetaData().getRelationMetaData();

         // if the table for the related entity has been created
         final EntityBridge relatedEntity = cmrField.getRelatedEntity();

         // Only generate indices on foreign key columns if
         // the table was freshly created. If not, we risk
         // creating an index twice and get an exception from the DB
         if(relationMetaData.isForeignKeyMappingStyle() && createdTables.contains(relatedEntity.getEntityName()))
         {
            createCMRIndex(((JDBCAbstractEntityBridge)relatedEntity).getDataSource(), cmrField);
         }

         // Create my fk constraint
         addForeignKeyConstraint(cmrField);
      }
   }

   public static Set getCreatedTables(JDBCEntityPersistenceStore manager)
   {
      final String key = "CREATED_TABLES";
      Set createdTables = (Set) manager.getApplicationData(key);
      if(createdTables == null)
      {
         createdTables = new HashSet();
         manager.putApplicationData(key, createdTables);
      }
      return createdTables;
   }

   public static Set getExistedTables(JDBCEntityPersistenceStore manager)
   {
      final String key = "EXISTED_TABLES";
      Set existedTables = (Set) manager.getApplicationData(key);
      if(existedTables == null)
      {
         existedTables = new HashSet();
         manager.putApplicationData(key, existedTables);
      }
      return existedTables;
   }

   /**
    * Check whether a required index already exists on a table
    *
    * @param oldIndexes list of existing indexes
    * @param field      field for we test the existence of an index
    * @return
    */
   private boolean hasIndex(SQLUtil.OldIndexes oldIndexes, JDBCFieldBridge field)
   {
      JDBCType jdbcType = field.getJDBCType();
      String[] columns = jdbcType.getColumnNames();
      ArrayList idxNames = oldIndexes.getIndexNames();
      ArrayList idxColumns = oldIndexes.getColumnNames();
      ArrayList idxAscDesc = oldIndexes.getColumnAscDesc();

      // search for for column in index
      for(int i = 0; i < idxColumns.size(); i++)
      {
         // only match ascending columns
         if(idxAscDesc.get(i).equals("A"))
         {
            String name = columns[0];
            String testCol = (String) idxColumns.get(i);
            if(testCol.equalsIgnoreCase(name))
            {
               // first column matches, now check the others
               String idxName = (String) idxNames.get(i);
               int j = 1;
               for(; j < columns.length; j++)
               {
                  name = columns[j];
                  testCol = (String) idxColumns.get(i + j);
                  String testName = (String) idxNames.get(i + j);
                  if(!(testName.equals(idxName)
                     &&
                     testCol.equalsIgnoreCase(name)
                     && idxAscDesc.get(i + j).equals("A")))
                  {
                     break;
                  }
               }
               // if they all matched -> found
               if(j == columns.length) return true;
            }
         }
      }
      return false;
   }

   private void alterTable(DataSource dataSource, JDBCFunctionMappingMetaData mapping, String tableName, String fieldName, String fieldStructure)
      throws DeploymentException
   {
      StringBuffer sqlBuf = new StringBuffer();
      mapping.getFunctionSql( new String[]{tableName, fieldName, fieldStructure}, sqlBuf );
      String sql = sqlBuf.toString();

      log.warn( sql );

      // suspend the current transaction
      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction;
      try
      {
         oldTransaction = tm.suspend();
      }
      catch(Exception e)
      {
         throw new DeploymentException(COULDNT_SUSPEND + " alter table.", e);
      }

      try
      {
         Connection con = null;
         Statement statement = null;
         try
         {
            con = dataSource.getConnection();
            statement = con.createStatement();
            statement.executeUpdate(sql.toString());
         }
         finally
         {
            // make sure to close the connection and statement before
            // comitting the transaction or XA will break
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }
      }
      catch(Exception e)
      {
         log.error("Could not alter table " + tableName + ": " + e.getMessage());
         throw new DeploymentException("Error while alter table " + tableName + " " + sql, e);
      }
      finally
      {
         try
         {
            // resume the old transaction
            if(oldTransaction != null)
            {
               tm.resume(oldTransaction);
            }
         }
         catch(Exception e)
         {
            throw new DeploymentException(COULDNT_REATTACH + "alter table");
         }
      }

      // success
      if ( log.isDebugEnabled() )
         log.debug("Table altered successfully.");
   }

   private void createTable(DataSource dataSource, String tableName, String sql)
      throws DeploymentException
   {
      // does this table already exist
      if(SQLUtil.tableExists(tableName, dataSource))
      {
         log.debug("Table '" + tableName + "' already exists");
         return;
      }

      // since we use the pools, we have to do this within a transaction

      // suspend the current transaction
      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction;
      try
      {
         oldTransaction = tm.suspend();
      }
      catch(Exception e)
      {
         throw new DeploymentException(COULDNT_SUSPEND + "creating table.", e);
      }

      try
      {
         Connection con = null;
         Statement statement = null;
         try
         {
            // execute sql
            if(log.isDebugEnabled())
            {
               log.debug("Executing SQL: " + sql);
            }

            con = dataSource.getConnection();
            statement = con.createStatement();
            statement.executeUpdate(sql);
         }
         finally
         {
            // make sure to close the connection and statement before
            // comitting the transaction or XA will break
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }
      }
      catch(Exception e)
      {
         log.debug("Could not create table " + tableName);
         throw new DeploymentException("Error while creating table " + tableName, e);
      }
      finally
      {
         try
         {
            // resume the old transaction
            if(oldTransaction != null)
            {
               tm.resume(oldTransaction);
            }
         }
         catch(Exception e)
         {
            throw new DeploymentException(COULDNT_REATTACH + "create table");
         }
      }

      // success
      Set createdTables = (Set) manager.getApplicationData(CREATED_TABLES_KEY);
      createdTables.add(tableName);
   }

   /**
    * Create an index on a field. Does the create
    *
    * @param dataSource
    * @param tableName  In which table is the index?
    * @param indexName  Which is the index?
    * @param sql        The SQL statement to issue
    * @throws DeploymentException
    */
   private void createIndex(DataSource dataSource, String tableName, String indexName, String sql)
      throws DeploymentException
   {
      // we are only called directly after creating a table
      // since we use the pools, we have to do this within a transaction
      // suspend the current transaction
      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction;
      try
      {
         oldTransaction = tm.suspend();
      }
      catch(Exception e)
      {
         throw new DeploymentException(COULDNT_SUSPEND + "creating index.", e);
      }

      try
      {
         Connection con = null;
         Statement statement = null;
         try
         {
            // execute sql
            if(log.isDebugEnabled())
            {
               log.debug("Executing SQL: " + sql);
            }
            con = dataSource.getConnection();
            statement = con.createStatement();
            statement.executeUpdate(sql);
         }
         finally
         {
            // make sure to close the connection and statement before
            // comitting the transaction or XA will break
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }
      }
      catch(Exception e)
      {
         log.debug("Could not create index " + indexName + "on table" + tableName);
         throw new DeploymentException("Error while creating table", e);
      }
      finally
      {
         try
         {
            // resume the old transaction
            if(oldTransaction != null)
            {
               tm.resume(oldTransaction);
            }
         }
         catch(Exception e)
         {
            throw new DeploymentException(COULDNT_REATTACH + "create index");
         }
      }
   }


   /**
    * Send (user-defined) SQL commands to the server.
    * The commands can be found in the &lt;sql-statement&gt; elements
    * within the &lt;post-table-create&gt; tag in jbossjdbc-cmp.xml
    *
    * @param dataSource
    */
   private void issuePostCreateSQL(DataSource dataSource, List sql, String table)
      throws DeploymentException
   {
      if(sql == null)
      { // no work to do.
         log.trace("issuePostCreateSQL: sql is null");
         return;
      }

      log.debug("issuePostCreateSQL::sql: " + sql.toString() + " on table " + table);

      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction;

      try
      {
         oldTransaction = tm.suspend();
      }
      catch(Exception e)
      {
         throw new DeploymentException(COULDNT_SUSPEND + "sending sql command.", e);
      }

      String currentCmd = "";

      try
      {
         Connection con = null;
         Statement statement = null;
         try
         {
            con = dataSource.getConnection();
            statement = con.createStatement();

            // execute sql
            for(int i = 0; i < sql.size(); i++)
            {
               currentCmd = (String) sql.get(i);
               /*
                * Replace %%t in the sql command with the current table name
                */
               currentCmd = replaceTable(currentCmd, table);
               currentCmd = replaceIndexCounter(currentCmd);
               log.debug("Executing SQL: " + currentCmd);
               statement.executeUpdate(currentCmd);
            }
         }
         finally
         {
            // make sure to close the connection and statement before
            // comitting the transaction or XA will break
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }
      }
      catch(Exception e)
      {
         log.warn("Issuing sql " + currentCmd + " failed: " + e.toString());
         throw new DeploymentException("Error while issuing sql in post-table-create", e);
      }
      finally
      {
         try
         {
            // resume the old transaction
            if(oldTransaction != null)
            {
               tm.resume(oldTransaction);
            }
         }
         catch(Exception e)
         {
            throw new DeploymentException(COULDNT_REATTACH + "create index");
         }
      }

      // success
      log.debug("Issued SQL  " + sql + " successfully.");
   }

   private String getEntityCreateTableSQL(DataSource dataSource)
      throws DeploymentException
   {
      StringBuffer sql = new StringBuffer();
      sql.append(SQLUtil.CREATE_TABLE).append(entity.getQualifiedTableName()).append(" (");

      // add fields
      boolean comma = false;
      JDBCFieldBridge[] fields = entity.getTableFields();
      for(int i = 0; i < fields.length; ++i)
      {
         JDBCFieldBridge field = fields[i];
         JDBCType type = field.getJDBCType();
         if(comma)
         {
            sql.append(SQLUtil.COMMA);
         }
         else
         {
            comma = true;
         }
         addField(type, sql);
      }

      // add a pk constraint
      if(entityMetaData.hasPrimaryKeyConstraint())
      {
         JDBCFunctionMappingMetaData pkConstraint = manager.getMetaData().getTypeMapping().getPkConstraintTemplate();
         if(pkConstraint == null)
         {
            throw new IllegalStateException("Primary key constraint is " +
               "not allowed for this type of data source");
         }

         String name = "pk_" + entity.getManager().getMetaData().getDefaultTableName();
         name = SQLUtil.fixConstraintName(name, dataSource);
         String[] args = new String[]{
            name,
            SQLUtil.getColumnNamesClause(entity.getPrimaryKeyFields(), new StringBuffer(100)).toString()
         };
         sql.append(SQLUtil.COMMA);
         pkConstraint.getFunctionSql(args, sql);
      }

      return sql.append(')').toString();
   }

   /**
    * Create indices for the fields in the table that have a
    * &lt;dbindex&gt; tag in jbosscmp-jdbc.xml
    *
    * @param dataSource
    * @throws DeploymentException
    */
   private void createCMPIndices(DataSource dataSource)
      throws DeploymentException
   {
      // Only create indices on CMP fields
      JDBCFieldBridge[] cmpFields = entity.getTableFields();
      for(int i = 0; i < cmpFields.length; ++i)
      {
         JDBCFieldBridge field = cmpFields[i];
         JDBCCMPFieldMetaData fieldMD = entity.getMetaData().getCMPFieldByName(field.getFieldName());

         if(fieldMD != null && fieldMD.isIndexed())
         {
            createCMPIndex(dataSource, field);
         }
      }

      final JDBCAbstractCMRFieldBridge[] cmrFields = entity.getCMRFields();
      if(cmrFields != null)
      {
         for(int i = 0; i < cmrFields.length; ++i)
         {
            JDBCAbstractCMRFieldBridge cmrField = cmrFields[i];
            if(cmrField.getRelatedCMRField().getMetaData().isIndexed())
            {
               final JDBCFieldBridge[] fkFields = cmrField.getForeignKeyFields();
               if(fkFields != null)
               {
                  for(int fkInd = 0; fkInd < fkFields.length; ++fkInd)
                  {
                     createCMPIndex(dataSource, fkFields[fkInd]);
                  }
               }
            }
         }
      }
   }

   /**
    * Create indix for one specific field
    *
    * @param dataSource
    * @param field      to create index for
    * @throws DeploymentException
    */
   private void createCMPIndex(DataSource dataSource, JDBCFieldBridge field)
      throws DeploymentException
   {
      StringBuffer sql;
      log.debug("Creating index for field " + field.getFieldName());
      sql = new StringBuffer();
      sql.append(SQLUtil.CREATE_INDEX);
      sql.append(entity.getQualifiedTableName() + IDX_POSTFIX + idxCount);// index name
      sql.append(SQLUtil.ON);
      sql.append(entity.getQualifiedTableName() + " (");
      SQLUtil.getColumnNamesClause(field, sql);
      sql.append(")");

      createIndex(dataSource,
         entity.getQualifiedTableName(),
         entity.getQualifiedTableName() + IDX_POSTFIX + idxCount,
         sql.toString());
      idxCount++;
   }

   private void createCMRIndex(DataSource dataSource, JDBCAbstractCMRFieldBridge field)
      throws DeploymentException
   {
      JDBCRelationMetaData rmd;
      String tableName;

      rmd = field.getMetaData().getRelationMetaData();

      if(rmd.isTableMappingStyle())
      {
         tableName = rmd.getDefaultTableName();
      }
      else
      {
         tableName = field.getRelatedCMRField().getEntity().getQualifiedTableName();
      }

      JDBCRelationshipRoleMetaData left, right;

      left = rmd.getLeftRelationshipRole();
      right = rmd.getRightRelationshipRole();

      Collection kfl = left.getKeyFields();
      JDBCCMPFieldMetaData fi;
      Iterator it = kfl.iterator();

      while(it.hasNext())
      {
         fi = (JDBCCMPFieldMetaData) it.next();
         if(left.isIndexed())
         {
            createIndex(dataSource, tableName, fi.getFieldName(), createIndexSQL(fi, tableName));
            idxCount++;
         }
      }

      Collection kfr = right.getKeyFields();
      it = kfr.iterator();
      while(it.hasNext())
      {
         fi = (JDBCCMPFieldMetaData) it.next();
         if(right.isIndexed())
         {
            createIndex(dataSource, tableName, fi.getFieldName(), createIndexSQL(fi, tableName));
            idxCount++;
         }
      }
   }

   private static String createIndexSQL(JDBCCMPFieldMetaData fi, String tableName)
   {
      StringBuffer sql = new StringBuffer();
      sql.append(SQLUtil.CREATE_INDEX);
      sql.append(fi.getColumnName() + IDX_POSTFIX + idxCount);
      sql.append(SQLUtil.ON);
      sql.append(tableName + " (");
      sql.append(fi.getColumnName());
      sql.append(')');
      return sql.toString();
   }

   private void addField(JDBCType type, StringBuffer sqlBuffer)
   {
      // apply auto-increment template
      if(type.getAutoIncrement()[0])
      {
         String columnClause = SQLUtil.getCreateTableColumnsClause(type);
         JDBCFunctionMappingMetaData autoIncrement =
            manager.getMetaData().getTypeMapping().getAutoIncrementTemplate();
         if(autoIncrement == null)
         {
            throw new IllegalStateException("auto-increment template not found");
         }
         String[] args = new String[]{columnClause};
         autoIncrement.getFunctionSql(args, sqlBuffer);
      }
      else
      {
         sqlBuffer.append(SQLUtil.getCreateTableColumnsClause(type));
      }
   }

   private String getRelationCreateTableSQL(JDBCAbstractCMRFieldBridge cmrField,
                                            DataSource dataSource)
      throws DeploymentException
   {
      JDBCFieldBridge[] leftKeys = cmrField.getTableKeyFields();
      JDBCFieldBridge[] rightKeys = cmrField.getRelatedCMRField().getTableKeyFields();
      JDBCFieldBridge[] fieldsArr = new JDBCFieldBridge[leftKeys.length + rightKeys.length];
      System.arraycopy(leftKeys, 0, fieldsArr, 0, leftKeys.length);
      System.arraycopy(rightKeys, 0, fieldsArr, leftKeys.length, rightKeys.length);

      StringBuffer sql = new StringBuffer();
      sql.append(SQLUtil.CREATE_TABLE).append(cmrField.getQualifiedTableName())
         .append(" (")
         // add field declaration
         .append(SQLUtil.getCreateTableColumnsClause(fieldsArr));

      // add a pk constraint
      final JDBCRelationMetaData relationMetaData = cmrField.getMetaData().getRelationMetaData();
      if(relationMetaData.hasPrimaryKeyConstraint())
      {
         JDBCFunctionMappingMetaData pkConstraint =
            manager.getMetaData().getTypeMapping().getPkConstraintTemplate();
         if(pkConstraint == null)
         {
            throw new IllegalStateException("Primary key constraint is not allowed for this type of data store");
         }

         String name = "pk_" + relationMetaData.getDefaultTableName();
         name = SQLUtil.fixConstraintName(name, dataSource);
         String[] args = new String[]{
            name,
            SQLUtil.getColumnNamesClause(fieldsArr, new StringBuffer(100).toString(), new StringBuffer()).toString()
         };
         sql.append(SQLUtil.COMMA);
         pkConstraint.getFunctionSql(args, sql);
      }
      sql.append(')');
      return sql.toString();
   }

   private void addForeignKeyConstraint(JDBCAbstractCMRFieldBridge cmrField)
      throws DeploymentException
   {
      JDBCRelationshipRoleMetaData metaData = cmrField.getMetaData();
      if(metaData.hasForeignKeyConstraint())
      {
         if(metaData.getRelationMetaData().isTableMappingStyle())
         {
            addForeignKeyConstraint(metaData.getRelationMetaData().getDataSource(),
               cmrField.getQualifiedTableName(),
               cmrField.getFieldName(),
               cmrField.getTableKeyFields(),
               cmrField.getEntity().getQualifiedTableName(),
               cmrField.getEntity().getPrimaryKeyFields());

         }
         else if(cmrField.hasForeignKey())
         {
            JDBCAbstractEntityBridge relatedEntity = (JDBCAbstractEntityBridge) cmrField.getRelatedEntity();
            addForeignKeyConstraint(cmrField.getEntity().getDataSource(),
               cmrField.getEntity().getQualifiedTableName(),
               cmrField.getFieldName(),
               cmrField.getForeignKeyFields(),
               relatedEntity.getQualifiedTableName(),
               relatedEntity.getPrimaryKeyFields());
         }
      }
      else
      {
         log.debug("Foreign key constraint not added as requested: relationshipRolename=" + metaData.getRelationshipRoleName());
      }
   }

   private void addForeignKeyConstraint(DataSource dataSource,
                                        String tableName,
                                        String cmrFieldName,
                                        JDBCFieldBridge[] fields,
                                        String referencesTableName,
                                        JDBCFieldBridge[] referencesFields) throws DeploymentException
   {
      // can only alter tables we created
      Set createdTables = (Set) manager.getApplicationData(CREATED_TABLES_KEY);
      if(!createdTables.contains(tableName))
      {
         return;
      }

      JDBCFunctionMappingMetaData fkConstraint = manager.getMetaData().getTypeMapping().getFkConstraintTemplate();
      if(fkConstraint == null)
      {
         throw new IllegalStateException("Foreign key constraint is not allowed for this type of datastore");
      }
      String a = SQLUtil.getColumnNamesClause(fields, new StringBuffer(50)).toString();
      String b = SQLUtil.getColumnNamesClause(referencesFields, new StringBuffer(50)).toString();

      String[] args = new String[]{
         tableName,
         SQLUtil.fixConstraintName("fk_" + tableName + "_" + cmrFieldName, dataSource),
         a,
         referencesTableName,
         b};

      String sql = fkConstraint.getFunctionSql(args, new StringBuffer(100)).toString();

      // since we use the pools, we have to do this within a transaction
      // suspend the current transaction
      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction;
      try
      {
         oldTransaction = tm.suspend();
      }
      catch(Exception e)
      {
         throw new DeploymentException(COULDNT_SUSPEND + "alter table create foreign key.", e);
      }

      try
      {
         Connection con = null;
         Statement statement = null;
         try
         {
            if(log.isDebugEnabled())
            {
               log.debug("Executing SQL: " + sql);
            }
            con = dataSource.getConnection();
            statement = con.createStatement();
            statement.executeUpdate(sql);
         }
         finally
         {
            // make sure to close the connection and statement before
            // comitting the transaction or XA will break
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }
      }
      catch(Exception e)
      {
         log.warn("Could not add foreign key constraint: table=" + tableName);
         throw new DeploymentException("Error while adding foreign key constraint", e);
      }
      finally
      {
         try
         {
            // resume the old transaction
            if(oldTransaction != null)
            {
               tm.resume(oldTransaction);
            }
         }
         catch(Exception e)
         {
            throw new DeploymentException(COULDNT_REATTACH + "create table");
         }
      }
   }


   /**
    * Replace %%t in the sql command with the current table name
    *
    * @param in    sql statement with possible %%t to substitute with table name
    * @param table the table name
    * @return String with sql statement
    */
   private static String replaceTable(String in, String table)
   {
      int pos;

      pos = in.indexOf("%%t");
      // No %%t -> return input
      if(pos == -1)
      {
         return in;
      }

      String first = in.substring(0, pos);
      String last = in.substring(pos + 3);

      return first + table + last;
   }

   /**
    * Replace %%n in the sql command with a running (index) number
    *
    * @param in
    * @return
    */
   private static String replaceIndexCounter(String in)
   {
      int pos;

      pos = in.indexOf("%%n");
      // No %%n -> return input
      if(pos == -1)
      {
         return in;
      }

      String first = in.substring(0, pos);
      String last = in.substring(pos + 3);
      idxCount++;
      return first + idxCount + last;
   }
}
