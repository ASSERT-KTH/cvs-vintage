/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMP2xFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
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
 * @author <a href="loubyansky@ua.fm">Alex Loubyansky</a>
 * @version $Revision: 1.34 $
 */
public class JDBCStartCommand {

   private final static Object CREATED_TABLES_KEY = new Object();
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private JDBCEntityMetaData entityMetaData;
   private Logger log;
   
   public JDBCStartCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();
      entityMetaData = entity.getMetaData();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());

      // Create the created tables set
      Map applicationData = manager.getApplicationDataMap();
      synchronized(applicationData) {
         if(!applicationData.containsKey(CREATED_TABLES_KEY)) {
            applicationData.put(
                  CREATED_TABLES_KEY,
                  Collections.synchronizedSet(new HashSet()));
         }
      }
   }

   public void execute() throws DeploymentException {

      // Create table if necessary
      if(!entity.getTableExists()) {
         if(entityMetaData.getCreateTable()) {
            DataSource dataSource = entity.getDataSource();
            createTable(
                  dataSource,
                  entity.getTableName(),
                  getEntityCreateTableSQL(dataSource));
         } else {
            log.debug("Table not create as requested: " +
                  entity.getTableName());
         }
         entity.setTableExists(true);
      }
     
      // create relation tables
      List cmrFields = entity.getCMRFields();
      for(Iterator iter = cmrFields.iterator(); iter.hasNext();) { 
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)iter.next();

         JDBCRelationMetaData relationMetaData = cmrField.getRelationMetaData();

         // if the table for the related entity has been created
         if(cmrField.getRelatedJDBCEntity().getTableExists()) {

            // create the relation table
            if(relationMetaData.isTableMappingStyle() &&
               !relationMetaData.getTableExists()) {
               
               if(relationMetaData.getCreateTable()) {
                  DataSource dataSource = relationMetaData.getDataSource();
                  createTable(
                        dataSource,
                        cmrField.getTableName(),
                        getRelationCreateTableSQL(cmrField, dataSource));
               } else {
                  log.debug("Relation table not create as requested: " +
                        cmrField.getTableName());
               }
            }
            relationMetaData.setTableExists(true);

            // Create my fk constraint
            addForeignKeyConstraint(cmrField);

            // Create related fk constraint
            if(!entity.equals(cmrField.getRelatedJDBCEntity())) {
               addForeignKeyConstraint(cmrField.getRelatedCMRField());
            }
         }
      }
   }

   private void createTable(
         DataSource dataSource,
         String tableName,
         String sql) throws DeploymentException {


      // does this table already exist
      if(tableExists(dataSource, tableName)) {
         log.info("Table '" + tableName + "' already exists");
         return;
      }

      // since we use the pools, we have to do this within a transaction

      // suspend the current transaction
      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction = null;
      try {
         oldTransaction = tm.suspend();
      } catch(Exception e) {
         throw new DeploymentException("Could not suspend current " +
               "transaction before creating table.", e);
      }

      try {
         Connection con = null;
         Statement statement = null;
         try {        
            con = dataSource.getConnection();
            statement = con.createStatement();
         
            // execute sql
            log.debug("Executing SQL: " + sql);
            statement.executeUpdate(sql);
         } finally {
            // make sure to close the connection and statement before 
            // comitting the transaction or XA will break
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }
      } catch(Exception e) {
         log.debug("Could not create table " + tableName);
         throw new DeploymentException("Error while creating table", e);
      } finally {
         try {
            // resume the old transaction
            if(oldTransaction != null) {
               tm.resume(oldTransaction);
            }
         } catch(Exception e) {
            throw new DeploymentException("Could not reattach original " +
                  "transaction after create table");
         }
      }

      // success
      log.info("Created table '" + tableName + "' successfully.");
      Set createdTables = (Set)manager.getApplicationData(CREATED_TABLES_KEY);
      createdTables.add(tableName);
   }

   private boolean tableExists(
         DataSource dataSource, 
         String tableName) throws DeploymentException {

      Connection con = null;
      ResultSet rs = null;
      try {
         con = dataSource.getConnection();

         // (a j2ee spec compatible jdbc driver has to fully 
         // implement the DatabaseMetaData)
         DatabaseMetaData dmd = con.getMetaData();
         rs = dmd.getTables(con.getCatalog(), null, tableName, null);
         return rs.next();
      } catch(SQLException e) {
         // This should not happen. A J2EE compatiable JDBC driver is
         // required fully support metadata.
         throw new DeploymentException("Error while checking if table aleady " +
               "exists", e);
      } finally {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(con);
      }
   }

   private String getEntityCreateTableSQL(DataSource dataSource) 
         throws DeploymentException {

      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(entity.getTableName()).append(" (");

      // add fields
      int columnCount = 0; // just to decide whether to sql.append(", ")
      for(Iterator iter = entity.getFields().iterator(); iter.hasNext();) {

         JDBCFieldBridge field = (JDBCFieldBridge) iter.next();
         JDBCType type = field.getJDBCType();

         // the side that doesn't have a foreign key has JDBCType null
         if(type==null)
            continue;

         // add foreign key fields unless they mapped to primary key fields
         if(field instanceof JDBCCMRFieldBridge) {
            JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)field;
            Iterator fkFieldIter = cmrField.getForeignKeyFields().iterator();
            while(fkFieldIter.hasNext()) {
               JDBCCMP2xFieldBridge fkField = (JDBCCMP2xFieldBridge)fkFieldIter.next();
               if(fkField.isFkFieldMappedToPkField())
                  continue;

               if(columnCount > 0)
                  sql.append( ", " );

               addField(fkField.getJDBCType(), sql);
               ++columnCount;
            }
         } else {
            if(columnCount > 0)
               sql.append( ", " );
            addField(type, sql);
            ++columnCount;
         }
      }

      // add a pk constraint
      if(entityMetaData.hasPrimaryKeyConstraint())  {
         JDBCFunctionMappingMetaData pkConstraint =
            manager.getMetaData().getTypeMapping().getPkConstraintTemplate();
         if(pkConstraint == null) {
            throw new IllegalStateException("Primary key constraint is " +
                  "not allowed for this type of data source");
         }

         String name = "pk_" + entity.getMetaData().getDefaultTableName();
         name = SQLUtil.fixConstraintName(name, dataSource);
         String[] args = new String[] {
            name,
            SQLUtil.getColumnNamesClause(entity.getPrimaryKeyFields())};
         sql.append(", ").append(pkConstraint.getFunctionSql(args));
      }

      sql.append(")");

      return sql.toString();
   }

   private void addField(JDBCType type, StringBuffer sqlBuffer) {
      // apply auto-increment template
      if(type.getAutoIncrement()[0]) {
         String columnClause =
         SQLUtil.getCreateTableColumnsClause( type );

         JDBCFunctionMappingMetaData autoIncrement =
            manager.getMetaData().getTypeMapping().
            getAutoIncrementTemplate();
         if(autoIncrement == null) {
            throw new IllegalStateException(
               "auto-increment template not found");
         }

         String[] args = new String[] { columnClause };
         sqlBuffer.append(autoIncrement.getFunctionSql(args));
      } else {
         sqlBuffer.append(SQLUtil.getCreateTableColumnsClause(type));
      }
   }

   private String getRelationCreateTableSQL(
         JDBCCMRFieldBridge cmrField,
         DataSource dataSource) throws DeploymentException {

      List fields = new ArrayList();
      fields.addAll(cmrField.getTableKeyFields());
      fields.addAll(cmrField.getRelatedCMRField().getTableKeyFields());

      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(
            cmrField.getTableName());

      sql.append(" (");
      // add field declaration
      sql.append(SQLUtil.getCreateTableColumnsClause(fields));

      // add a pk constraint
      if(cmrField.getRelationMetaData().hasPrimaryKeyConstraint())  {
         JDBCFunctionMappingMetaData pkConstraint =
            manager.getMetaData().getTypeMapping().getPkConstraintTemplate();
         if(pkConstraint == null) {
            throw new IllegalStateException("Primary key constraint is " +
                  "not allowed for this type of data store");
         }

         String name = "pk_" + cmrField.getRelationMetaData().getDefaultTableName();
         name = SQLUtil.fixConstraintName(name, dataSource);
         String[] args = new String[] {
            name,
            SQLUtil.getColumnNamesClause(fields)};
         sql.append(", ").append(pkConstraint.getFunctionSql(args));
      }
      sql.append(")");

      return sql.toString();
   }

   private void addForeignKeyConstraint(JDBCCMRFieldBridge cmrField)
         throws DeploymentException {
      if(cmrField.getMetaData().hasForeignKeyConstraint()) {

         if(cmrField.getRelationMetaData().isTableMappingStyle()) {
            addForeignKeyConstraint(
                  cmrField.getRelationMetaData().getDataSource(),
                  cmrField.getTableName(),
                  cmrField.getFieldName(),
                  cmrField.getTableKeyFields(),
                  cmrField.getEntity().getTableName(),
                  cmrField.getEntity().getPrimaryKeyFields());

         } else if(cmrField.hasForeignKey()) {
            addForeignKeyConstraint(
                  cmrField.getEntity().getDataSource(),
                  cmrField.getEntity().getTableName(),
                  cmrField.getFieldName(),
                  cmrField.getForeignKeyFields(),
                  cmrField.getRelatedJDBCEntity().getTableName(),
                  cmrField.getRelatedJDBCEntity().getPrimaryKeyFields());
         }
      } else {
         log.debug("Foreign key constraint not added as requested: " +
               "relationshipRolename=" +
               cmrField.getMetaData().getRelationshipRoleName());
      }
   }

   private void addForeignKeyConstraint(
         DataSource dataSource,
         String tableName,
         String cmrFieldName,
         List fields,
         String referencesTableName,
         List referencesFields) throws DeploymentException {

      // can only alter tables we created
      Set createdTables = (Set)manager.getApplicationData(CREATED_TABLES_KEY);
      if(!createdTables.contains(tableName)) {
         return;
      }

      JDBCFunctionMappingMetaData fkConstraint =
            manager.getMetaData().getTypeMapping().getFkConstraintTemplate();
      if(fkConstraint == null) {
         throw new IllegalStateException("Foreign key constraint is not " +
               "allowed for this type of datastore");
      }
      String a = SQLUtil.getColumnNamesClause(fields);
      String b = SQLUtil.getColumnNamesClause(referencesFields);


      String[] args = new String[] {
         tableName,
         SQLUtil.fixConstraintName(
               "fk_"+tableName+"_"+cmrFieldName, dataSource),
         a,
         referencesTableName,
         b};
      String sql = fkConstraint.getFunctionSql(args);

      // since we use the pools, we have to do this within a transaction
      // suspend the current transaction
      TransactionManager tm = manager.getContainer().getTransactionManager();
      Transaction oldTransaction = null;
      try {
         oldTransaction = tm.suspend();
      } catch(Exception e) {
         throw new DeploymentException("Could not suspend current " +
               "transaction before alter table create foreign key.", e);
      }

      try {
         Connection con = null;
         Statement statement = null;
         try {
            // get the connection
            con = dataSource.getConnection();

            // create the statement
            statement = con.createStatement();

            // execute sql
            log.debug("Executing SQL: " + sql);
            statement.executeUpdate(sql);
         } finally {
            // make sure to close the connection and statement before
            // comitting the transaction or XA will break
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         }
      } catch(Exception e) {
         log.warn("Could not add foreign key constraint: table=" + tableName);
         throw new DeploymentException("Error while adding foreign key " +
               "constraint", e);
      } finally {
         try {
            // resume the old transaction
            if(oldTransaction != null) {
               tm.resume(oldTransaction);
            }
         } catch(Exception e) {
            throw new DeploymentException("Could not reattach original " +
                  "transaction after create table");
         }
      }

      // success
      log.info("Added foreign key constraint to table '" + tableName + "'" );
   }
}
