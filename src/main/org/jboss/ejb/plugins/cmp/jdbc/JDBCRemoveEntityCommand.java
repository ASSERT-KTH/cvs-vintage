/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.rmi.RemoteException;

import java.sql.PreparedStatement;

import javax.ejb.RemoveException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.RemoveEntityCommand;

/**
 * JDBCRemoveEntityCommand executes a DELETE FROM table WHERE command.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCRemoveEntityCommand
   extends JDBCUpdateCommand
   implements RemoveEntityCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCRemoveEntityCommand(JDBCStoreManager manager)
   {
      super(manager, "Remove");
      
		StringBuffer sql = new StringBuffer();
      sql.append("DELETE ");
		sql.append("FROM ").append(entityMetaData.getTableName());
		sql.append(" WHERE ").append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));
		
      setSQL(sql.toString());
   }
   
   // RemoveEntityCommand implementation -------------------------
   
	public void execute(EntityEnterpriseContext context)
			throws RemoteException, RemoveException {

		try {
			jdbcExecute(context.getId());
		} catch (Exception e) {
			throw new RemoveException("Could not remove " + context.getId());
		}
	}
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected void setParameters(PreparedStatement ps, Object primaryKey) throws Exception {
      entity.setPrimaryKeyParameters(ps, 1, primaryKey);
   }
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) throws Exception {
      if(rowsAffected == 0) {
         throw new RemoveException("Could not remove entity");
      }
		return null;
   }
}
