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
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.deployment.DeploymentException;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Method;

/**
 * JDBCBeanExistsCommand is a JDBC query that checks if an id exists
 * in the database.  This is used by the create and findByPrimaryKey
 * code.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.9 $
 */
public final class JDBCFindByPrimaryKeyQuery extends JDBCAbstractQueryCommand
{
   private JDBCStoreManager manager;
   private boolean rowLocking;

   public JDBCFindByPrimaryKeyQuery(JDBCStoreManager manager, JDBCQueryMetaData q)
      throws DeploymentException
   {
      super(manager, q);
      this.manager = manager;
      rowLocking = manager.getMetaData().hasRowLocking();

      JDBCEntityBridge entity = manager.getEntityBridge();

      JDBCTypeMappingMetaData typeMapping = this.manager.getJDBCTypeFactory().getTypeMapping();
      AliasManager aliasManager = new AliasManager(
         typeMapping.getAliasHeaderPrefix(),
         typeMapping.getAliasHeaderSuffix(),
         typeMapping.getAliasMaxLength()
      );

      String alias = aliasManager.getAlias(entity.getEntityName());

      StringBuffer select = new StringBuffer(200);
      SQLUtil.getColumnNamesClause(entity.getPrimaryKeyFields(), alias, select);

      StringBuffer from = new StringBuffer();
      from.append(entity.getTableName())
         .append(' ')
         .append(alias);

      // set the preload fields
      JDBCReadAheadMetaData readAhead = q.getReadAhead();
      if(readAhead.isOnFind())
      {
         setEagerLoadGroup(readAhead.getEagerLoadGroup());
         if(getEagerLoadMask() != null)
         {
            SQLUtil.appendColumnNamesClause(entity.getTableFields(), getEagerLoadMask(), alias, select);

            List onFindCMRList = JDBCAbstractQueryCommand.getLeftJoinCMRNodes(
               entity, entity.getTableName(), readAhead.getLeftJoins()
            );

            if(!onFindCMRList.isEmpty())
            {
               setOnFindCMRList(onFindCMRList);
               JDBCAbstractQueryCommand.leftJoinCMRNodes(alias, onFindCMRList, aliasManager, from);
               JDBCAbstractQueryCommand.appendLeftJoinCMRColumnNames(onFindCMRList, aliasManager, select);
            }
         }
      }

      StringBuffer where = new StringBuffer();
      SQLUtil.getWhereClause(entity.getPrimaryKeyFields(), alias, where);

      // generate the sql
      StringBuffer sql = new StringBuffer(300);
      if(rowLocking && readAhead.isOnFind() && getEagerLoadMask() != null)
      {
         JDBCFunctionMappingMetaData rowLockingTemplate = typeMapping.getRowLockingTemplate();
         rowLockingTemplate.getFunctionSql(
            new Object[]{
               select,
               from,
               where.length() == 0 ? null : where,
               null // order by
            },
            sql
         );
      }
      else
      {
         sql.append(SQLUtil.SELECT)
            .append(select)
            .append(SQLUtil.FROM)
            .append(from)
            .append(SQLUtil.WHERE)
            .append(where);
      }

      setSQL(sql.toString());
      setParameterList(QueryParameter.createPrimaryKeyParameters(0, entity));
   }

   public Collection execute(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws FinderException
   {
      // Check in readahead cache.
      if(rowLocking && manager.getReadAheadCache().getPreloadDataMap(args[0], false) != null)
      {
         return Collections.singletonList(args[0]);
      }

      Collection results = super.execute(finderMethod, args, ctx);

      if(results.isEmpty())
      {
         throw new ObjectNotFoundException("No such entity!");
      }

      return results;
   }
}
