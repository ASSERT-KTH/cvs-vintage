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
import javax.ejb.EJBException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
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
 * @version $Revision: 1.19 $
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
            createTable(
                  entity.getDataSource(),
                  entity.getTableName(),
                  getEntityCreateTableSQL());
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
         if(cmrField.getRelatedEntity().getTableExists()) {

            // create the relation table
            if(relationMetaData.isTableMappingStyle() &&
               !relationMetaData.getTableExists()) {
               
               if(relationMetaData.getCreateTable()) {
                  createTable(
                        relationMetaData.getDataSource(),
                        relationMetaData.getTableName(),
                        getRelationCreateTableSQL(cmrField));
               } else {
                  log.debug("Relation table not create as requested: " +
                        relationMetaData.getTableName());
               }
            }
            relationMetaData.setTableExists(true);

            // Create my fk constraint
            addForeignKeyConstraint(cmrField);

            // Create related fk constraint
            addForeignKeyConstraint(cmrField.getRelatedCMRField());
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

      Connection con = null;
      Statement statement = null;

      // since we use the pools, we have to do this within a transaction
      try 
      {
         manager.getContainer().getTransactionManager().begin ();         
      }
      catch (Exception e)
      {
         log.error("Could not get transaction to create table in", e);
         throw new DeploymentException("Could not get transaction to create table in", e);
      } // end of try-catch
      try {
         // get the connection
         con = dataSource.getConnection();
         try {        
            // create the statement
            statement = con.createStatement();
         
         // execute sql
            log.debug("Executing SQL: " + sql);
            statement.executeUpdate(sql);
         }
         finally
         {
            JDBCUtil.safeClose(statement);
            JDBCUtil.safeClose(con);
         } // end of finally
         manager.getContainer().getTransactionManager().commit ();
      } 
      catch(Exception e) 
      {
         log.debug("Could not create table " + tableName);
         try {
            manager.getContainer().getTransactionManager().rollback ();
         } catch(Exception _e) {
            log.error("Could not roll back transaction: ", e);
         }
         throw new DeploymentException("Error while creating table", e);
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

   private String getEntityCreateTableSQL() {

      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(entityMetaData.getTableName());
      
      sql.append(" (");
         // add fields
         sql.append(SQLUtil.getCreateTableColumnsClause(entity.getFields()));
         
         // add a pk constraint
         if(entityMetaData.hasPrimaryKeyConstraint())  {
            JDBCFunctionMappingMetaData pkConstraint = 
               manager.getMetaData().getTypeMapping().getPkConstraintTemplate();
            if(pkConstraint == null) {
               throw new IllegalStateException("Primary key constriant is " +
                     "not allowed for this type of datastore");
            }
            String[] args = new String[] {
               "pk_"+entityMetaData.getTableName(),
               SQLUtil.getColumnNamesClause(entity.getPrimaryKeyFields())};
            sql.append(", ").append(pkConstraint.getFunctionSql(args));
         }

      sql.append(")");
      
      return sql.toString();
   }

   private String getRelationCreateTableSQL(JDBCCMRFieldBridge cmrField) {
      List fields = new ArrayList();
      fields.addAll(cmrField.getTableKeyFields());
      fields.addAll(cmrField.getRelatedCMRField().getTableKeyFields());

      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(
            cmrField.getRelationMetaData().getTableName());
      
      sql.append(" (");
         // add field declaration
         sql.append(SQLUtil.getCreateTableColumnsClause(fields));

         // add a pk constraint
         if(cmrField.getRelationMetaData().hasPrimaryKeyConstraint())  {
            JDBCFunctionMappingMetaData pkConstraint = 
               manager.getMetaData().getTypeMapping().getPkConstraintTemplate();
            if(pkConstraint == null) {
               throw new IllegalStateException("Primary key constriant is " +
                     "not allowed for this type of datastore");
            }
            String[] args = new String[] {
               "pk_"+cmrField.getRelationMetaData().getTableName(),
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
                  cmrField.getRelationMetaData().getTableName(),
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
                  cmrField.getRelatedEntity().getTableName(),
                  cmrField.getRelatedEntity().getPrimaryKeyFields());
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
         throw new IllegalStateException("Foreign key constriant is not " +
               "allowed for this type of datastore");
      }
      String a = SQLUtil.getColumnNamesClause(fields);
      String b = SQLUtil.getColumnNamesClause(referencesFields);
      String[] args = new String[] {
         tableName, 
         "fk_"+tableName+"_"+cmrFieldName,
         a,
         referencesTableName,
         b};
      String sql = fkConstraint.getFunctionSql(args);

      Connection con = null;
      Statement statement = null;
      try {
         // since we use the pools, we have to do this within a transaction
         manager.getContainer().getTransactionManager().begin();

         // get the connection
         con = dataSource.getConnection();
         
         // create the statement
         statement = con.createStatement();
         
         // execute sql
         log.debug("Executing SQL: " + sql);
         statement.executeUpdate(sql);

         // commit the transaction
         manager.getContainer().getTransactionManager().commit();

         // success
         log.info("Added foreign key constriant to table '" + tableName);
      } catch(Exception e) {
         log.debug("Could not add foreign key constriant: table=" + tableName);
         try {
            manager.getContainer().getTransactionManager().rollback ();
         } catch (Exception _e) {
            log.error("Could not roll back transaction: ", e);
         }  
         throw new DeploymentException("Error while adding foreign key " +
               "constraint", e);
      } finally {
         JDBCUtil.safeClose(statement);
         JDBCUtil.safeClose(con);
      }
   }
}
