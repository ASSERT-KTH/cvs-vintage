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
import org.jboss.ejb.plugins.cmp.StartCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

/**
 * JDBCStartCommand creates the table if specified in xml.
 *    
  * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 1.7 $
 */
public class JDBCStartCommand extends JDBCUpdateCommand implements StartCommand {
   // Constructors --------------------------------------------------
   private boolean alreadyCreated = false;
   
   public JDBCStartCommand(JDBCStoreManager manager) {
      super(manager, "Start");
   }   

   // InitCommand implementation ---------------------------------

   public void execute() throws Exception {
      // Create table if necessary
      if(!alreadyCreated || entityMetaData.getCreateTable()) {
         createTable(entityMetaData.getTableName(), getEntityCreateTableSQL());
         
         // create relation tables
         JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
         for(int i=0; i<cmrFields.length; i++) {
            //Verify that there is a related CMR field (to avoid NPE)
            if(!cmrFields[i].hasForeignKey() 
                  && cmrFields[i].getRelatedCMRField() != null 
                  && !cmrFields[i].getRelatedCMRField().hasForeignKey()) {
               createTable(cmrFields[i].getRelationTableName(), getRelationCreateTableSQL(cmrFields[i]));
            }
         }

         alreadyCreated = true;
      }
   }

   protected void createTable(String tableName, String sql) throws Exception {
      // first check if the table already exists...
      // (a j2ee spec compatible jdbc driver has to fully 
      // implement the DatabaseMetaData)
      Connection con = null;
      ResultSet rs = null;
      try {
         con = manager.getConnection();
         DatabaseMetaData dmd = con.getMetaData();
         rs = dmd.getTables(con.getCatalog(), null, tableName, null);
         if (rs.next ()) {
            log.info("Table '" + tableName + "' already exists");
            return;
         }
      } finally {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(con);
      }

      try {
         // since we use the pools, we have to do this within a transaction
         manager.getContainer().getTransactionManager().begin ();
         jdbcExecute(sql);
         manager.getContainer().getTransactionManager().commit ();
         
         // Create successful, log this
         log.info("Created table '" + tableName + "' successfully.");
       } catch (Exception e) {
         log.debug("Could not create table " + tableName, e);
         try {
            manager.getContainer().getTransactionManager().rollback ();
         } catch (Exception _e) {
            log.error("Could not roll back transaction: ", e);
         }
      }
   }

   protected String getEntityCreateTableSQL() throws Exception {
      // Create table SQL
      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(entityMetaData.getTableName());
      
      sql.append(" (");
         // list all cmp fields
         sql.append(SQLUtil.getCreateTableColumnsClause(entity.getJDBCCMPFields()));
         
         // append foriegn key fields
         JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
         for(int i=0; i<cmrFields.length; i++) {
            if(cmrFields[i].hasForeignKey()) {
               sql.append(", ").append(SQLUtil.getCreateTableColumnsClause(cmrFields[i].getForeignKeyFields()));
            }
         }

         // If there is a primary key field,
         // and the bean has explicitly <pk-constraint>true</pk-constraint> in jbosscmp-jdbc.xml
         // add primary key constraint.
         if(entityMetaData.hasPrimaryKeyConstraint())  {
            sql.append(", CONSTRAINT pk").append(entityMetaData.getTableName());
            
            sql.append(" PRIMARY KEY (");
               sql.append(SQLUtil.getColumnNamesClause(entity.getJDBCPrimaryKeyFields()));
            sql.append(")");
         }   
      sql.append(")");
      
      return sql.toString();
   }

   protected String getRelationCreateTableSQL(JDBCCMRFieldBridge cmrField) throws Exception {
      // Create table SQL
      StringBuffer sql = new StringBuffer();
      sql.append("CREATE TABLE ").append(cmrField.getRelationTableName());
      
      sql.append(" (");
         // list all cmp fields
         sql.append(SQLUtil.getCreateTableColumnsClause(cmrField.getTableKeyFields()));
         sql.append(", ");      
         sql.append(SQLUtil.getCreateTableColumnsClause(cmrField.getRelatedCMRField().getTableKeyFields()));

         // If there is a primary key field,
         // and the bean has explicitly <pk-constraint>true</pk-constraint> in jbosscmp-jdbc.xml
         // add primary key constraint.
//         if(cmrField.hasPrimaryKeyConstraint())  {
//            sql.append(", CONSTRAINT pk").append(cmrField.getRelationTableName());
//            
//            sql.append(" PRIMARY KEY (");
//               sql.append(SQLUtil.getColumnNamesClause(cmrField.getTableKeyFields())));
//               sql.append(", ");      
//               sql.append(SQLUtil.getColumnNamesClause(cmrField.getRelatedCMRField().getTableKeyFields()));
//            sql.append(")");
//         }   
      sql.append(")");
      
      return sql.toString();
   }

   protected String getSQL(Object sql) throws Exception {
      return (String) sql;
   }
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) throws Exception {
      return null;
   }
}
