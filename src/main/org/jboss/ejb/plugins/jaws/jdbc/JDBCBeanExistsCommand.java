/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * JDBCBeanExistsCommand
 *
 * @see <related>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
public class JDBCBeanExistsCommand extends JDBCQueryCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCBeanExistsCommand(JDBCCommandFactory factory)
   {
      super(factory, "Exists");
      String sql = "SELECT COUNT(*) AS Total FROM " + jawsEntity.getTableName() +
                   " WHERE " + getPkColumnWhereList();
      setSQL(sql);
   }
   
   // Public --------------------------------------------------------
   
   // Checks whether the database already holds the entity
   
   public boolean execute(Object id)
   {
      boolean result = false;
      
      try
      {
         result = ((Boolean)jdbcExecute(id)).booleanValue();
      } catch (Exception e)
      {
         log.exception(e);
      }
      
      return result;
   }
   
   // JDBCQueryCommand overrides ------------------------------------
   
   protected void setParameters(PreparedStatement stmt, Object argOrArgs) 
      throws Exception
   {
      setPrimaryKeyParameters(stmt, 1, argOrArgs);
   }
   
   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception
   {
      if ( !rs.next() )
      {
         throw new SQLException("Unable to check for EJB in database");
      }
      int total = rs.getInt("Total");
      return new Boolean(total >= 1);
   }
}
