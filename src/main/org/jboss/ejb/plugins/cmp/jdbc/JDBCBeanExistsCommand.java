/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * JDBCBeanExistsCommand is a JDBC query that checks if an id exists
 * in the database.  This is used by the create and findByPrimaryKey
 * code.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.5 $
 */
public class JDBCBeanExistsCommand extends JDBCQueryCommand
{
   // Constructors --------------------------------------------------

   public JDBCBeanExistsCommand(JDBCStoreManager manager)
   {
      super(manager, "Exists");

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT COUNT(*) ");
      sql.append("FROM ").append(entityMetaData.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));
      
      setSQL(sql.toString());
   }

   // Public --------------------------------------------------------

   // Checks whether the database already holds the entity

   public boolean execute(Object primaryKeyObject) throws Exception {
      return ((Boolean)jdbcExecute(primaryKeyObject)).booleanValue();
   }

   // JDBCQueryCommand overrides ------------------------------------

   protected void setParameters(PreparedStatement ps, Object primaryKey) throws Exception {                 
      entity.setPrimaryKeyParameters(ps, 1, primaryKey);
   }

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception {
      if( !rs.next() ) {
         throw new SQLException("Unable to check for EJB in database");
      }
      
      // Do we have atleast one matching row?
      int total = rs.getInt(1);
      return new Boolean(total >= 1);
   }
}
