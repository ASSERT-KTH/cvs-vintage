/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCJBossQLQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;

/**
 * This class generates a query from JBoss-QL.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.10 $
 */
public final class JDBCJBossQLQuery extends JDBCAbstractQueryCommand
{

   public JDBCJBossQLQuery(JDBCStoreManager manager,
                           JDBCQueryMetaData q)
      throws DeploymentException
   {
      super(manager, q);

      JDBCJBossQLQueryMetaData metadata = (JDBCJBossQLQueryMetaData)q;
      if(getLog().isDebugEnabled())
      {
         getLog().debug("JBossQL: " + metadata.getJBossQL());
      }

      QLCompiler compiler = JDBCQueryManager.getInstance(metadata.getQLCompilerClass(), manager.getCatalog());

      try
      {
         compiler.compileJBossQL(
            metadata.getJBossQL(),
            metadata.getMethod().getReturnType(),
            metadata.getMethod().getParameterTypes(),
            metadata);
      }
      catch(Throwable t)
      {
         t.printStackTrace();
         throw new DeploymentException("Error compiling JBossQL " +
            "statement '" + metadata.getJBossQL() + "'", t);
      }

      setSQL(compiler.getSQL());
      setOffsetParam(compiler.getOffsetParam());
      setOffsetValue(compiler.getOffsetValue());
      setLimitParam(compiler.getLimitParam());
      setLimitValue(compiler.getLimitValue());

      // set select object
      if(compiler.isSelectEntity())
      {
         JDBCEntityBridge selectEntity = (JDBCEntityBridge) compiler.getSelectEntity();

         // set the select entity
         setSelectEntity(selectEntity);

         // set the preload fields
         JDBCReadAheadMetaData readahead = metadata.getReadAhead();
         if(readahead.isOnFind())
         {
            setEagerLoadGroup(readahead.getEagerLoadGroup());
            setOnFindCMRList(compiler.getLeftJoinCMRList());
         }
      }
      else if(compiler.isSelectField())
      {
         setSelectField((JDBCCMPFieldBridge) compiler.getSelectField());
      }
      else
      {
         setSelectFunction(compiler.getSelectFunction(), (JDBCStoreManager) compiler.getStoreManager());
      }

      // get the parameter order
      setParameterList(compiler.getInputParameters());
   }
}
