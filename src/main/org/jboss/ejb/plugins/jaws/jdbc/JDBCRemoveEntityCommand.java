/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.rmi.RemoteException;

import java.sql.PreparedStatement;

import javax.ejb.RemoveException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JPMRemoveEntityCommand;

/**
 * JAWSPersistenceManager JDBCRemoveEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public class JDBCRemoveEntityCommand
   extends JDBCUpdateCommand
   implements JPMRemoveEntityCommand
{
   // Attributes ----------------------------------------------------
   
   private EntityEnterpriseContext ctxArgument;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public JDBCRemoveEntityCommand(JDBCCommandFactory factory)
   {
      super(factory, "Remove");
      
      // Remove SQL
      String sql = "DELETE FROM " + metaInfo.getTableName() +
                   " WHERE "+getPkColumnWhereList();
      setSQL(sql);
   }
   
   // JPMRemoveEntityCommand implementation -------------------------
   
   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      ctxArgument = ctx;
      
      try
      {
         // Remove from DB
         jdbcExecute();
      } catch (Exception e)
      {
         throw new RemoveException("Could not remove "+ctx.getId());
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected void setParameters(PreparedStatement stmt) throws Exception
   {
      setPrimaryKeyParameters(stmt, 1, ctxArgument.getId());
   }
   
   protected void handleResult(int rowsAffected) throws Exception
   {
      if (rowsAffected == 0)
      {
         throw new RemoveException("Could not remove entity");
      }
   }
}
