/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jboss.ejb.plugins.cmp.DestroyCommand;

/**
 * JDBCDestroyCommand drops the table for this entity if specified in the xml.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCDestroyCommand extends JDBCUpdateCommand implements DestroyCommand {
   // Constructors --------------------------------------------------
   
   public JDBCDestroyCommand(JDBCStoreManager manager) {
      super(manager, "Destroy");
      
      // Drop table SQL
      String sql = "DROP TABLE " + entityMetaData.getTableName();
      setSQL(sql);
   }
   
   // DestroyCommand implementation ------------------------------
   
   public void execute() {
      if(entityMetaData.getRemoveTable()) {
         try {
            // since we use the pools, we have to do this within a transaction
            manager.getContainer().getTransactionManager().begin ();
            jdbcExecute(null);
            manager.getContainer().getTransactionManager().commit ();
         } catch (Exception e) {
            log.debug("Could not drop table " + entityMetaData.getTableName() + ": " + e.getMessage());
            try {
               manager.getContainer().getTransactionManager().rollback ();
            } catch (Exception _e) {
               log.error("Could not roll back transaction: "+ _e.getMessage());
            }
         }
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) 
      throws Exception
   {
      log.debug("Table " + entityMetaData.getTableName() + " removed");
      
      return null;
   }
}
