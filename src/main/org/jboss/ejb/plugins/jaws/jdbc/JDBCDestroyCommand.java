/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jboss.ejb.plugins.jaws.JPMDestroyCommand;
import org.jboss.ejb.plugins.jaws.MetaInfo;

/**
 * JAWSPersistenceManager JDBCDestroyCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
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
      String sql = "DROP TABLE " + metaInfo.getTableName();
      setSQL(sql);
   }
   
   // JPMDestroyCommand implementation ------------------------------
   
   public void execute()
   {
      if (metaInfo.getRemoveTable())
      {
         // Remove it!
         try
         {
            jdbcExecute(null);
         } catch (Exception e)
         {
            log.debug("Could not drop table " +
                      metaInfo.getTableName() + ": " + e.getMessage());
         }
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) 
      throws Exception
   {
      log.debug("Table "+metaInfo.getTableName()+" removed");
      
      return null;
   }
}
