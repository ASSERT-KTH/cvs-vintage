/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jboss.ejb.plugins.jaws.JPMDestroyCommand;

/**
 * JAWSPersistenceManager JDBCDestroyCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.7 $
 */
public class JDBCDestroyCommand
   extends JDBCUpdateCommand
   implements JPMDestroyCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCDestroyCommand(JDBCCommandFactory factory)
   {
      super(factory, "Destroy");
      
      // Drop table SQL
      String sql = "DROP TABLE " + jawsEntity.getTableName();
      setSQL(sql);
   }
   
   // JPMDestroyCommand implementation ------------------------------
   
   public void execute()
   {
      if (jawsEntity.getRemoveTable())
      {
         // Remove it!
         try
         {
            // since we use the pools, we have to do this within a transaction
            factory.getContainer().getTransactionManager().begin ();
            jdbcExecute(null);
            factory.getContainer().getTransactionManager().commit ();
         } catch (Exception e)
         {
            log.debug("Could not drop table " +
                      jawsEntity.getTableName() + ": " + e.getMessage());

            try
            {
               factory.getContainer().getTransactionManager().rollback ();
            }
            catch (Exception _e)
            {
               log.error("Could not roll back transaction: "+ _e.getMessage());
            }
         }
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) 
      throws Exception
   {
      log.debug("Table "+jawsEntity.getTableName()+" removed");
      
      return null;
   }
}
