/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
/**
 * JDBCFindAllQuery automatic finder used in CMP 1.x.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCFindAllQuery extends JDBCAbstractQueryCommand {
   
   public JDBCFindAllQuery(JDBCStoreManager manager, JDBCQueryMetaData q) {
      super(manager, q);
      
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(
               manager.getEntityBridge().getJDBCPrimaryKeyFields()));
      sql.append(" FROM ").append(manager.getEntityBridge().getTableName());
      
      setSQL(sql.toString());
   }
}
