/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;

/**
 * JDBCFindAllQuery automatic finder used in CMP 1.x.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.8 $
 */
public final class JDBCFindAllQuery extends JDBCAbstractQueryCommand
{

   public JDBCFindAllQuery(JDBCStoreManager manager, JDBCQueryMetaData q)
   {
      super(manager, q);

      JDBCEntityBridge entity = (JDBCEntityBridge) manager.getEntityBridge();

      // set the preload fields
      JDBCReadAheadMetaData readAhead = q.getReadAhead();
      if(readAhead.isOnFind())
      {
         setEagerLoadGroup(readAhead.getEagerLoadGroup());
      }

      // generate the sql
      StringBuffer sql = new StringBuffer(300);
      sql.append(SQLUtil.SELECT);
      // put pk fields first
      SQLUtil.getColumnNamesClause(entity.getPrimaryKeyFields(), sql);
      if(getEagerLoadGroup() != null)
      {
         SQLUtil.appendColumnNamesClause(entity, getEagerLoadGroup(), sql);
      }
      sql.append(SQLUtil.FROM).append(entity.getQualifiedTableName());

      setSQL(sql.toString());
   }
}
