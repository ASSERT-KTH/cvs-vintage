/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.Iterator;
import javax.ejb.FinderException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.LocalProxyFactory;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCLeftJoinMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.ejb.plugins.cmp.ejbql.SelectFunction;
import org.jboss.logging.Logger;

/**
 * Abstract superclass of finder commands that return collections.
 * Provides the handleResult() implementation that these all need.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 *
 * @version $Revision: 1.24 $
 */
public abstract class JDBCAbstractQueryCommand implements JDBCQueryCommand
{
   // todo: get rid of it
   private static final String FINDER_PREFIX = "find";
   private JDBCQueryMetaData queryMetaData;
   protected Logger log;

   private JDBCStoreManager selectManager;
   private JDBCEntityBridge selectEntity;
   private JDBCCMPFieldBridge selectField;
   private SelectFunction selectFunction;
   private boolean[] eagerLoadMask;
   private String eagerLoadGroup;
   private String sql;
   private int offsetParam;
   private int offsetValue;
   private int limitParam;
   private int limitValue;
   private List parameters = new ArrayList(0);
   private List onFindCMRList = Collections.EMPTY_LIST;

   public JDBCAbstractQueryCommand(JDBCStoreManager manager, JDBCQueryMetaData q)
   {
      this.log = Logger.getLogger(
         this.getClass().getName() +
         "." +
         manager.getMetaData().getName() +
         "#" +
         q.getMethod().getName());

      queryMetaData = q;
//      setDefaultOffset(q.getOffsetParam());
//      setDefaultLimit(q.getLimitParam());
      setSelectEntity(manager.getEntityBridge());
   }

   public void setOffsetValue(int offsetValue)
   {
      this.offsetValue = offsetValue;
   }

   public void setLimitValue(int limitValue)
   {
      this.limitValue = limitValue;
   }

   public void setOffsetParam(int offsetParam)
   {
      this.offsetParam = offsetParam;
   }

   public void setLimitParam(int limitParam)
   {
      this.limitParam = limitParam;
   }

   public void setOnFindCMRList(List onFindCMRList)
   {
      this.onFindCMRList = onFindCMRList;
   }

   public Collection execute(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws FinderException
   {
      int offset = toInt(args, offsetParam, offsetValue);
      int limit = toInt(args, limitParam, limitValue);
      return execute(
         sql,
         args,
         offset,
         limit,
         selectEntity,
         selectField,
         selectFunction,
         selectManager,
         eagerLoadMask,
         parameters,
         onFindCMRList,
         queryMetaData,
         log
      );
   }

   protected static int toInt(Object[] params, int paramNumber, int defaultValue)
   {
      if(paramNumber == 0)
         return defaultValue;
      Integer arg = (Integer)params[paramNumber - 1];
      return arg.intValue();
   }

   protected static Collection execute(String sql,
                                     Object[] args,
                                     int offset,
                                     int limit,
                                     JDBCEntityBridge selectEntity,
                                     JDBCCMPFieldBridge selectField,
                                     SelectFunction selectFunction,
                                     JDBCStoreManager selectManager,
                                     boolean[] eagerLoadMask,
                                     List parameters,
                                     List onFindCMRList,
                                     JDBCQueryMetaData queryMetaData,
                                     Logger log)
      throws FinderException
   {
      ReadAheadCache selectReadAheadCache = null;
      if(selectEntity != null)
      {
         selectReadAheadCache = selectManager.getReadAheadCache();
      }

      List results = new ArrayList();

      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
         // create the statement
         if(log.isDebugEnabled())
         {
            log.debug("Executing SQL: " + sql);
            if(limit != 0 || offset != 0)
            {
               log.debug("Query offset=" + offset + ", limit=" + limit);
            }
         }

         // get the connection
         con = selectManager.getEntityBridge().getDataSource().getConnection();
         ps = con.prepareStatement(sql);

         // Set the fetch size of the statement
         if(selectManager.getEntityBridge().getFetchSize() > 0)
         {
            ps.setFetchSize(selectManager.getEntityBridge().getFetchSize());
         }

         // set the parameters
         for(int i = 0; i < parameters.size(); i++)
         {
            QueryParameter parameter = (QueryParameter)parameters.get(i);
            parameter.set(log, ps, i + 1, args);
         }

         // execute statement
         rs = ps.executeQuery();

         // skip 'offset' results
         int count = offset;
         while(count > 0 && rs.next())
         {
            count--;
         }

         count = limit;

         // load the results
         if(selectEntity != null)
         {
            boolean loadOnFindCmr = !onFindCMRList.isEmpty();
            Object[] ref = new Object[1];
            Object prevPk = null;
            while((limit == 0 || count-- > 0) && rs.next())
            {
               int index = 1;

               // get the pk
               index = selectEntity.loadPrimaryKeyResults(rs, index, ref);
               Object pk = ref[0];

               // note: loaded pk might be null
               boolean addPk = (loadOnFindCmr ? !pk.equals(prevPk) : true);
               if(addPk)
               {
                  results.add(pk);
                  prevPk = pk;
               }

               // read the preload fields
               if(eagerLoadMask != null)
               {
                  JDBCCMPFieldBridge[] tableFields = (JDBCCMPFieldBridge[])selectEntity.getTableFields();
                  for(int i = 0; i < eagerLoadMask.length; i++)
                  {
                     if(eagerLoadMask[i])
                     {
                        JDBCCMPFieldBridge field = tableFields[i];
                        ref[0] = null;

                        // read the value and store it in the readahead cache
                        index = field.loadArgumentResults(rs, index, ref);

                        if(addPk)
                           selectReadAheadCache.addPreloadData(pk, field, ref[0]);
                     }
                  }

                  if(!onFindCMRList.isEmpty())
                  {
                     index = loadOnFindCMRFields(pk, onFindCMRList, rs, index, log);
                  }
               }
            }
         }
         else if(selectField != null)
         {
            // load the field
            Object[] valueRef = new Object[1];
            while((limit == 0 || count-- > 0) && rs.next())
            {
               valueRef[0] = null;
               selectField.loadArgumentResults(rs, 1, valueRef);
               results.add(valueRef[0]);
            }
         }
         else
         {
            while(rs.next())
               results.add(selectFunction.readResult(rs));
         }

         if(log.isDebugEnabled() && limit != 0 && count == 0)
         {
            log.debug("Query result was limited to " + limit + " row(s)");
         }
      }
      catch(Exception e)
      {
         log.debug("Find failed", e);
         throw new FinderException("Find failed: " + e);
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // If we were just selecting a field, we're done.
      if(selectField != null || selectFunction != null)
      {
         return results;
      }

      // add the results list to the cache
      JDBCReadAheadMetaData readAhead = queryMetaData.getReadAhead();
      selectReadAheadCache.addFinderResults(results, readAhead);

      // If this is a finder, we're done.
      if(queryMetaData.getMethod().getName().startsWith(FINDER_PREFIX))
      {
         return results;
      }

      // This is an ejbSelect, so we need to convert the pks to real ejbs.
      EntityContainer selectContainer = selectManager.getContainer();
      if(queryMetaData.isResultTypeMappingLocal())
      {
         LocalProxyFactory localFactory = selectContainer.getLocalProxyFactory();
         return localFactory.getEntityLocalCollection(results);
      }
      else
      {
         EJBProxyFactory factory = selectContainer.getProxyFactory();
         return factory.getEntityCollection(results);
      }
   }

   protected Logger getLog()
   {
      return log;
   }

   protected void setSQL(String sql)
   {
      this.sql = sql;
      if(log.isDebugEnabled())
      {
         log.debug("SQL: " + sql);
      }
   }

   protected void setParameterList(List p)
   {
      for(int i = 0; i < p.size(); i++)
      {
         if(!(p.get(i) instanceof QueryParameter))
         {
            throw new IllegalArgumentException("Element " + i + " of list " +
               "is not an instance of QueryParameter, but " +
               p.get(i).getClass().getName());
         }
      }
      parameters = new ArrayList(p);
   }

   protected JDBCEntityBridge getSelectEntity()
   {
      return selectEntity;
   }

   protected void setSelectEntity(JDBCEntityBridge selectEntity)
   {
      this.selectField = null;
      this.selectFunction = null;
      this.selectEntity = selectEntity;
      this.selectManager = (JDBCStoreManager)selectEntity.getManager();
   }

   protected JDBCCMPFieldBridge getSelectField()
   {
      return selectField;
   }

   protected void setSelectField(JDBCCMPFieldBridge selectField)
   {
      this.selectEntity = null;
      this.selectFunction = null;
      this.selectField = selectField;
      this.selectManager = (JDBCStoreManager)selectField.getManager();
   }

   protected void setSelectFunction(SelectFunction func, JDBCStoreManager manager)
   {
      this.selectEntity = null;
      this.selectField = null;
      this.selectFunction = func;
      this.selectManager = manager;
   }

   protected void setEagerLoadGroup(String eagerLoadGroup)
   {
      this.eagerLoadGroup = eagerLoadGroup;
      this.eagerLoadMask = selectEntity.getLoadGroupMask(eagerLoadGroup);
   }

   protected String getEagerLoadGroup()
   {
      return eagerLoadGroup;
   }

   protected boolean[] getEagerLoadMask()
   {
      return this.eagerLoadMask;
   }

   /**
    * Replaces the parameters in the specifiec sql with question marks, and
    * initializes the parameter setting code. Parameters are encoded in curly
    * brackets use a zero based index.
    * @param sql the sql statement that is parsed for parameters
    * @return the original sql statement with the parameters replaced with a
    *    question mark
    * @throws DeploymentException if a error occures while parsing the sql
    */
   protected String parseParameters(String sql) throws DeploymentException
   {
      StringBuffer sqlBuf = new StringBuffer();
      ArrayList params = new ArrayList();

      // Replace placeholders {0} with ?
      if(sql != null)
      {
         sql = sql.trim();

         StringTokenizer tokens = new StringTokenizer(sql, "{}", true);
         while(tokens.hasMoreTokens())
         {
            String token = tokens.nextToken();
            if(token.equals("{"))
            {
               token = tokens.nextToken();
               if(Character.isDigit(token.charAt(0)))
               {
                  QueryParameter parameter = new QueryParameter(
                     selectManager,
                     queryMetaData.getMethod(),
                     token);

                  // of if we are here we can assume that we have
                  // a parameter and not a function
                  sqlBuf.append("?");
                  params.add(parameter);
                  if(!tokens.nextToken().equals("}"))
                  {
                     throw new DeploymentException("Invalid parameter - missing closing '}' : " + sql);
                  }
               }
               else
               {
                  // ok we don't have a parameter, we have a function
                  // push the tokens on the buffer and continue
                  sqlBuf.append("{").append(token);
               }
            }
            else
            {
               // not parameter... just append it
               sqlBuf.append(token);
            }
         }
      }

      parameters = params;
      return sqlBuf.toString();
   }

   // Static

   public static List getLeftJoinCMRNodes(JDBCEntityBridge entity, String path, Iterator leftJoinIter)
      throws DeploymentException
   {
      List leftJoinCMRNodes;

      if(leftJoinIter.hasNext())
      {
         leftJoinCMRNodes = new ArrayList();
         while(leftJoinIter.hasNext())
         {
            JDBCLeftJoinMetaData leftJoin = (JDBCLeftJoinMetaData)leftJoinIter.next();
            JDBCCMRFieldBridge cmrField = entity.getCMRFieldByName(leftJoin.getCmrField());
            if(cmrField == null)
            {
               throw new DeploymentException(
                  "cmr-field in left-join was not found: cmr-field=" +
                  leftJoin.getCmrField() + ", entity=" + entity.getEntityName()
               );
            }

            List subNodes;
            JDBCEntityBridge relatedEntity = cmrField.getRelatedJDBCEntity();
            String childPath = path + '.' + cmrField.getFieldName();
            subNodes = getLeftJoinCMRNodes(relatedEntity, childPath, leftJoin.getLeftJoins());

            boolean[] mask = relatedEntity.getLoadGroupMask(leftJoin.getEagerLoadGroup());
            LeftJoinCMRNode node = new LeftJoinCMRNode(childPath, cmrField, mask, subNodes);
            leftJoinCMRNodes.add(node);
         }
      }
      else
      {
         leftJoinCMRNodes = Collections.EMPTY_LIST;
      }

      return leftJoinCMRNodes;
   }

   public static final void leftJoinCMRNodes(String alias,
                                             List onFindCMRNodes,
                                             AliasManager aliasManager,
                                             StringBuffer sb)
   {
      for(int i = 0; i < onFindCMRNodes.size(); ++i)
      {
         LeftJoinCMRNode node = (LeftJoinCMRNode)onFindCMRNodes.get(i);
         JDBCCMRFieldBridge cmrField = node.cmrField;
         JDBCEntityBridge relatedEntity = cmrField.getRelatedJDBCEntity();
         String relatedAlias = aliasManager.getAlias(node.path);

         JDBCRelationMetaData relation = cmrField.getMetaData().getRelationMetaData();
         if(relation.isTableMappingStyle())
         {
            String relTableAlias = aliasManager.getRelationTableAlias(node.path);
            sb.append(" LEFT OUTER JOIN ")
               .append(cmrField.getTableName())
               .append(' ')
               .append(relTableAlias)
               .append(" ON ");
            SQLUtil.getRelationTableJoinClause(cmrField, alias, relTableAlias, sb);

            sb.append(" LEFT OUTER JOIN ")
               .append(relatedEntity.getTableName())
               .append(' ')
               .append(relatedAlias)
               .append(" ON ");
            SQLUtil.getRelationTableJoinClause(cmrField.getRelatedCMRField(), relatedAlias, relTableAlias, sb);
         }
         else
         {
            // foreign key mapping style
            sb.append(" LEFT OUTER JOIN ")
               .append(relatedEntity.getTableName())
               .append(' ')
               .append(relatedAlias)
               .append(" ON ");
            SQLUtil.getJoinClause(
               cmrField,
               alias,
               relatedAlias,
               sb
            );
         }

         List subNodes = node.onFindCMRNodes;
         if(!subNodes.isEmpty())
         {
            leftJoinCMRNodes(relatedAlias, subNodes, aliasManager, sb);
         }
      }
   }

   public static final void appendLeftJoinCMRColumnNames(List onFindCMRNodes,
                                                         AliasManager aliasManager,
                                                         StringBuffer sb)
   {
      for(int i = 0; i < onFindCMRNodes.size(); ++i)
      {
         LeftJoinCMRNode node = (LeftJoinCMRNode)onFindCMRNodes.get(i);
         JDBCCMRFieldBridge cmrField = node.cmrField;
         JDBCEntityBridge relatedEntity = cmrField.getRelatedJDBCEntity();
         String childAlias = aliasManager.getAlias(node.path);

         // primary key fields
         SQLUtil.appendColumnNamesClause(
            relatedEntity.getPrimaryKeyFields(),
            childAlias,
            sb
         );

         // eager load group
         if(node.eagerLoadMask != null)
         {
            SQLUtil.appendColumnNamesClause(
               relatedEntity.getTableFields(),
               node.eagerLoadMask,
               childAlias,
               sb
            );
         }

         List subNodes = node.onFindCMRNodes;
         if(!subNodes.isEmpty())
         {
            appendLeftJoinCMRColumnNames(subNodes, aliasManager, sb);
         }
      }
   }

   private static int loadOnFindCMRFields(Object pk, List onFindCMRNodes, ResultSet rs, int index, Logger log)
   {
      Object[] ref = new Object[1];
      for(int nodeInd = 0; nodeInd < onFindCMRNodes.size(); ++nodeInd)
      {
         LeftJoinCMRNode node = (LeftJoinCMRNode)onFindCMRNodes.get(nodeInd);
         JDBCCMRFieldBridge cmrField = node.cmrField;
         ReadAheadCache myCache = cmrField.getJDBCStoreManager().getReadAheadCache();
         JDBCEntityBridge relatedEntity = cmrField.getRelatedJDBCEntity();
         ReadAheadCache relatedCache = cmrField.getRelatedManager().getReadAheadCache();

         // load related id
         ref[0] = null;
         index = relatedEntity.loadPrimaryKeyResults(rs, index, ref);
         Object relatedId = ref[0];
         boolean cacheRelatedData = relatedId != null;

         if(pk != null)
         {
            if(cmrField.getMetaData().getRelatedRole().isMultiplicityOne())
            {
               // cacheRelatedData the value
               myCache.addPreloadData(
                  pk,
                  cmrField,
                  relatedId == null ? Collections.EMPTY_LIST : Collections.singletonList(relatedId)
               );
            }
            else
            {
               Collection cachedValue = myCache.getCachedCMRValue(pk, cmrField);
               if(cachedValue == null)
               {
                  cachedValue = new ArrayList();
                  myCache.addPreloadData(pk, cmrField, cachedValue);
               }

               if(relatedId != null)
               {
                  if(cachedValue.contains(relatedId))
                  {
                     cacheRelatedData = false;
                  }
                  else
                  {
                     cachedValue.add(relatedId);
                  }
               }
            }
         }

         // load eager load group
         if(node.eagerLoadMask != null)
         {
            JDBCCMPFieldBridge[] tableFields = (JDBCCMPFieldBridge[])relatedEntity.getTableFields();
            for(int fieldInd = 0; fieldInd < tableFields.length; ++fieldInd)
            {
               if(node.eagerLoadMask[fieldInd])
               {
                  JDBCCMPFieldBridge field = tableFields[fieldInd];
                  ref[0] = null;
                  index = field.loadArgumentResults(rs, index, ref);

                  if(cacheRelatedData)
                  {
                     if(log.isTraceEnabled())
                     {
                        log.trace(
                           "Caching " + relatedEntity.getEntityName() +
                           '[' + relatedId + "]." +
                           field.getFieldName() + "=" + ref[0]
                        );
                     }
                     relatedCache.addPreloadData(relatedId, field, ref[0]);
                  }
               }
            }
         }

         List subNodes = node.onFindCMRNodes;
         if(!subNodes.isEmpty())
         {
            index = loadOnFindCMRFields(relatedId, subNodes, rs, index, log);
         }
      }

      return index;
   }

   public static final class LeftJoinCMRNode
   {
      public final String path;
      public final JDBCCMRFieldBridge cmrField;
      public final boolean[] eagerLoadMask;
      public final List onFindCMRNodes;

      public LeftJoinCMRNode(String path, JDBCCMRFieldBridge cmrField, boolean[] eagerLoadMask, List onFindCMRNodes)
      {
         this.path = path;
         this.cmrField = cmrField;
         this.eagerLoadMask = eagerLoadMask;
         this.onFindCMRNodes = onFindCMRNodes;
      }

      public boolean equals(Object o)
      {
         boolean result;
         if(o == this)
         {
            result = true;
         }
         else if(o instanceof LeftJoinCMRNode)
         {
            LeftJoinCMRNode other = (LeftJoinCMRNode)o;
            result = cmrField == other.cmrField;
         }
         else
         {
            result = false;
         }
         return result;
      }

      public int hashCode()
      {
         return cmrField == null ? Integer.MIN_VALUE : cmrField.hashCode();
      }

      public String toString()
      {
         return '[' + cmrField.getFieldName() + ": " + onFindCMRNodes + ']';
      }
   }
}
