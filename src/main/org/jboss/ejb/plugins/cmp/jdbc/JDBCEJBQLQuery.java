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
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQlQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;

/**
 * This class generates a query from EJB-QL.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.15 $
 */
public final class JDBCEJBQLQuery extends JDBCAbstractQueryCommand
{

   public JDBCEJBQLQuery(JDBCStoreManager manager,
                         JDBCQueryMetaData q)
      throws DeploymentException
   {
      super(manager, q);

      JDBCQlQueryMetaData metadata = (JDBCQlQueryMetaData) q;
      if(getLog().isDebugEnabled())
      {
         getLog().debug("EJB-QL: " + metadata.getEjbQl());
      }

      QLCompiler compiler = JDBCQueryManager.getInstance(metadata.getQLCompilerClass(), manager.getCatalog());

      try
      {
         compiler.compileEJBQL(
            metadata.getEjbQl(),
            metadata.getMethod().getReturnType(),
            metadata.getMethod().getParameterTypes(),
            metadata.getReadAhead());
      }
      catch(Throwable t)
      {
         t.printStackTrace();
         throw new DeploymentException("Error compiling EJB-QL statement '" + metadata.getEjbQl() + "'", t);
      }

      setSQL(compiler.getSQL());

      // set select object
      if(compiler.isSelectEntity())
      {
         JDBCEntityBridge selectEntity = (JDBCEntityBridge)compiler.getSelectEntity();

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
         setSelectField((JDBCCMPFieldBridge)compiler.getSelectField());
      }
      else
      {
         setSelectFunction(compiler.getSelectFunction(), manager);
      }

      // get the parameter order
      setParameterList(compiler.getInputParameters());
   }
}
