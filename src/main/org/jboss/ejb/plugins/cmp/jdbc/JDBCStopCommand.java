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
import java.sql.SQLException;
import org.jboss.ejb.plugins.cmp.StopCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

/**
 * JDBCStopCommand drops the table for this entity if specified in the xml.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
public class JDBCStopCommand extends JDBCUpdateCommand implements StopCommand {

   public JDBCStopCommand(JDBCStoreManager manager) {
      super(manager, "Stop");
   }
   
   public void execute() {
      if(entityMetaData.getRemoveTable()) {
         log.debug("Droping tables for entity " + entity.getEntityName());
         dropTable(entityMetaData.getTableName());

         // drop relation tables
         JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
         for(int i=0; i<cmrFields.length; i++) {
            // if it uses a relation-table drop it
            if(!cmrFields[i].hasForeignKey() && 
                  !cmrFields[i].getRelatedCMRField().hasForeignKey()) {

               dropTable(cmrFields[i].getRelationTableName());
            }
         }
      }
   }
   
   public void dropTable(String tableName) {
      Connection con = null;
      ResultSet rs = null;
      try {
         con = manager.getConnection();
         DatabaseMetaData dmd = con.getMetaData();
         rs = dmd.getTables(con.getCatalog(), null, tableName, null);
         if(!rs.next()) {
            // table already deleted
            return;
         }
      } catch(SQLException e) {
         log.debug("Error getting database metadata for DROP TABLE command " +
               e);
         return;
      } finally {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(con);
      }

      try {
         // since we use the pools, we have to do this within a transaction
         manager.getContainer().getTransactionManager().begin();
         jdbcExecute("DROP TABLE " + tableName);
         manager.getContainer().getTransactionManager().commit();
         log.info("Dropped table '" + tableName + "' successfully.");
      } catch (Exception e) {
         log.debug("Could not drop table " + tableName + ": " + e.getMessage());
         try {
            manager.getContainer().getTransactionManager().rollback ();
         } catch (Exception _e) {
            log.error("Could not roll back transaction: "+ _e.getMessage());
         }
      }
   }
   
   protected String getSQL(Object sql) throws Exception {
      return (String) sql;
   }
      
   protected Object handleResult(int rowsAffected, Object argOrArgs) 
      throws Exception
   {     
      return null;
   }
}
