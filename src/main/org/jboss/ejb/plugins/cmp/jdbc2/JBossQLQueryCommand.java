/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMPFieldBridge2;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCJBossQLQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.QLCompiler;
import org.jboss.ejb.plugins.cmp.jdbc.EJBQLToSQL92Compiler;
import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class JBossQLQueryCommand
   extends AbstractQueryCommand
{
   public JBossQLQueryCommand(JDBCEntityBridge2 entity, JDBCJBossQLQueryMetaData metadata) throws DeploymentException
   {
      this.entity = entity;

      JDBCStoreManager2 manager = (JDBCStoreManager2)entity.getManager();
      QLCompiler compiler = new EJBQLToSQL92Compiler(manager.getCatalog());
      
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
         throw new DeploymentException("Error compiling JBossQL statement '" + metadata.getJBossQL() + "'", t);
      }

      sql = compiler.getSQL();

      log = Logger.getLogger(getClass().getName() + "." + entity.getEntityName() + "#" + metadata.getMethod().getName());
      log.debug("sql: " + sql);

      setParameters(compiler.getInputParameters());
      setResultType(metadata.getMethod().getReturnType());

      if(!compiler.isSelectEntity())
      {
         if(compiler.isSelectField())
         {
            setFieldReader((JDBCCMPFieldBridge2)compiler.getSelectField());
         }
         else
         {
            setFunctionReader(compiler.getSelectFunction());
         }
      }
      else
      {
         setEntityReader((JDBCEntityBridge2)compiler.getSelectEntity());
      }
   }
}
