/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Collections;
import javax.ejb.FinderException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.ejbql.SelectFunction;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCDynamicQLQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;

/**
 * This class generates a query from JBoss-QL.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.10 $
 */
public final class JDBCDynamicQLQuery extends JDBCAbstractQueryCommand
{
   private final Catalog catalog;
   private final JDBCDynamicQLQueryMetaData metadata;

   public JDBCDynamicQLQuery(JDBCStoreManager manager, JDBCQueryMetaData q)
      throws DeploymentException
   {
      super(manager, q);
      catalog = manager.getCatalog();
      metadata = (JDBCDynamicQLQueryMetaData)q;
   }

   public Collection execute(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws FinderException
   {
      String dynamicQL = (String)args[0];
      if(getLog().isDebugEnabled())
      {
         getLog().debug("DYNAMIC-QL: " + dynamicQL);
      }

      QLCompiler compiler = null;
      try
      {
         compiler = JDBCQueryManager.getInstance(metadata.getQLCompilerClass(), catalog);
      }
      catch(DeploymentException e)
      {
         throw new FinderException(e.getMessage());
      }

      // get the parameters
      Object[] parameters = (Object[])args[1];
      // parameter types
      Class[] parameterTypes;
      if(parameters == null)
      {
         parameterTypes = new Class[0];
      }
      else
      {
         // get the parameter types
         parameterTypes = new Class[parameters.length];
         for(int i = 0; i < parameters.length; i++)
         {
            if(parameters[i] == null)
            {
               throw new FinderException("Parameter[" + i + "] is null");
            }
            parameterTypes[i] = parameters[i].getClass();
         }
      }

      // compile the dynamic-ql
      try
      {
         compiler.compileJBossQL(
            dynamicQL,
            finderMethod.getReturnType(),
            parameterTypes,
            metadata.getReadAhead());
      }
      catch(Throwable t)
      {
         t.printStackTrace();
         throw new FinderException("Error compiling ejbql: " + t);
      }

      int offset = toInt(parameters, compiler.getOffsetParam(), compiler.getOffsetValue());
      int limit = toInt(parameters, compiler.getLimitParam(), compiler.getLimitValue());

      JDBCEntityBridge selectEntity = null;
      JDBCCMPFieldBridge selectField = null;
      SelectFunction selectFunction = null;
      if(compiler.isSelectEntity())
      {
         selectEntity = (JDBCEntityBridge)compiler.getSelectEntity();
      }
      else if(compiler.isSelectField())
      {
         selectField = (JDBCCMPFieldBridge)compiler.getSelectField();
      }
      else
      {
         selectFunction = compiler.getSelectFunction();
      }

      boolean[] mask;
      List leftJoinCMRList;
      JDBCReadAheadMetaData readahead = metadata.getReadAhead();
      if(selectEntity != null && readahead.isOnFind())
      {
         mask = selectEntity.getLoadGroupMask(readahead.getEagerLoadGroup());
         leftJoinCMRList = compiler.getLeftJoinCMRList();
      }
      else
      {
         mask = null;
         leftJoinCMRList = Collections.EMPTY_LIST;
      }

      // get the parameter order
      setParameterList(compiler.getInputParameters());

      return execute(
         compiler.getSQL(),
         parameters,
         offset,
         limit,
         selectEntity,
         selectField,
         selectFunction,
         (JDBCStoreManager)compiler.getStoreManager(),
         mask,
         compiler.getInputParameters(),
         leftJoinCMRList,
         metadata,
         log
      );
   }
}
