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
import javax.sql.DataSource;
import javax.ejb.EJBException;

import org.jboss.ejb.plugins.cmp.StartCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
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
 * @version $Revision: 1.9 $
 */
public class JDBCStartCommand implements StartCommand {

   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private JDBCEntityMetaData entityMetaData;
   private Logger log;
   private boolean alreadyCreated = false;
   
   public JDBCStartCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();
      entityMetaData = entity.getMetaData();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }

   public void execute() throws Exception {
      // Create table if necessary
      if(!alreadyCreated || entityMetaData.getCreateTable()) {
         createTable(
               manager.getDataSource(),
               entityMetaData.getTableName(),
               getEntityCreateTableSQL());

         alreadyCreated = true;
      } 

      // create relation tables
      JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
      for(int i=0; i<cmrFields.length; i++) {
         JDBCRelationMetaData relationMetaData = 
               cmrFields[i].getRelationMetaData();

         if(relationMetaData.isTableMappingStyle() &&
            !relationMetaData.getTableExists()) {
            
            if(relationMetaData.getCreateTable()) {
               createTable(
                     relationMetaData.getDataSource(),
                     relationMetaData.getTableName(),
                     getRelationCreateTableSQL(cmrFields[i]));
            } else {
               log.info("Table not create as requested: " +
                     relationMetaData.getTableName());
            }
            
            relationMetaData.setTableExists(true);
         }
      }
   }

   private void createTable(
         DataSource dataSource,
         String tableName,
         String sql) {

      // does this table already exist
      if(tableExists(dataSource, tableName)) {
         log.info("Table '" + tableName + "' already exists");
         return;
      }

      Connection con = null;
      Statement statement = null;
      try {
         // since we use the pools, we have to do this within a transaction
         manager.getContainer().getTransactionManager().begin ();

         // get the connection
         con = dataSource.getConnection();
         
         // create the statement
         statement = con.createStatement();
         
         // execute sql
         log.debug("Executing SQL: " + sql);
         statement.executeUpdate(sql);

         // commit the transaction
         manager.getContainer().getTransactionManager().commit ();

         // success
         log.info("Created table '" + tableName + "' successfully.");
      } catch (Exception e) {
         log.debug("Could not create table " + tableName, e);
         try {
            manager.getContainer().getTransactionManager().rollback ();
         } catch (Exception _e) {
            log.error("Could not roll back transaction: ", e);
         }
      } finally {
         JDBCUtil.safeClose(statement);
         JDBCUtil.safeClose(con);
      }
   }

   private boolean tableExists(
         DataSource dataSource, 
         String tableName) {

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
         throw new EJBException("Error while checking if table aleady " +
               "exists: ", e);
      } finally {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(con);
      }
   }

   private String getEntityCreateTableSQL() {

      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(entityMetaData.getTableName());
      
      sql.append(" (");
         // add cmp fields
         sql.append(SQLUtil.getCreateTableColumnsClause(
                  entity.getJDBCCMPFields()));
         
         // add foriegn key fields
         JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
         for(int i=0; i<cmrFields.length; i++) {
            if(cmrFields[i].hasForeignKey()) {
               sql.append(", ").append(SQLUtil.getCreateTableColumnsClause(
                     cmrFields[i].getForeignKeyFields()));
            }
         }

         // add a pk constraint
         if(entityMetaData.hasPrimaryKeyConstraint())  {
            sql.append(", CONSTRAINT pk").append(entityMetaData.getTableName());
            
            sql.append(" PRIMARY KEY (");
               sql.append(SQLUtil.getColumnNamesClause(
                        entity.getJDBCPrimaryKeyFields()));
            sql.append(")");
         }   
      sql.append(")");
      
      return sql.toString();
   }

   private String getRelationCreateTableSQL(JDBCCMRFieldBridge cmrField) {

      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(
            cmrField.getRelationMetaData().getTableName());
      
      sql.append(" (");
         // add cmr table key fields
         sql.append(SQLUtil.getCreateTableColumnsClause(
                  cmrField.getTableKeyFields()));

         // add related cmr table key fields
         sql.append(", ");      
         sql.append(SQLUtil.getCreateTableColumnsClause(
                  cmrField.getRelatedCMRField().getTableKeyFields()));

         // add a pk constraint
         if(cmrField.getRelationMetaData().hasPrimaryKeyConstraint())  {
            sql.append(", CONSTRAINT pk").append(
                  cmrField.getRelationMetaData().getTableName());
            
            sql.append(" PRIMARY KEY (");
               sql.append(SQLUtil.getColumnNamesClause(
                     cmrField.getTableKeyFields()));
               sql.append(", ");      
               sql.append(SQLUtil.getColumnNamesClause(
                     cmrField.getRelatedCMRField().getTableKeyFields()));
            sql.append(")");
         }   
      sql.append(")");
      
      return sql.toString();
   }
}
