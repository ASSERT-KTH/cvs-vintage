/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;

/**
 * JDBCBeanExistsCommand is a JDBC query that checks if an id exists
 * in the database.  This is used by the create and findByPrimaryKey
 * code.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCFindByPrimaryKeyQuery extends JDBCAbstractQueryCommand {

   public JDBCFindByPrimaryKeyQuery(
         JDBCStoreManager manager, JDBCQueryMetaData q) {

      super(manager, q);

      JDBCEntityBridge entity = manager.getEntityBridge();

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(
               entity.getJDBCPrimaryKeyFields()));
      sql.append("FROM ").append(entity.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(
               entity.getJDBCPrimaryKeyFields()));
      
      setSQL(sql.toString());
      setParameterList(QueryParameter.createParameters(0, entity));
   }
}
