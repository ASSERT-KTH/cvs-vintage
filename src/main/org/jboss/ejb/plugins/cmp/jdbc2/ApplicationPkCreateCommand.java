/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.deployment.DeploymentException;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public class ApplicationPkCreateCommand
   implements CreateCommand
{
   private JDBCEntityBridge2 entityBridge;

   public void init(JDBCStoreManager2 manager) throws DeploymentException
   {
      this.entityBridge = (JDBCEntityBridge2) manager.getEntityBridge();
   }

   public Object execute(Method m, Object[] args, EntityEnterpriseContext ctx) throws CreateException
   {
      Object pk;
      PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
      if(ctx.getId() == null)
      {
         pk = entityBridge.extractPrimaryKeyFromInstance(ctx);

         if(pk == null)
         {
            throw new CreateException("Primary key for created instance is null.");
         }

         pctx.setPk(pk);
      }
      else
      {
         // insert-after-ejb-post-create
         try
         {
            pctx.flush();
         }
         catch(SQLException e)
         {
            if("23000".equals(e.getSQLState()))
            {
               throw new DuplicateKeyException("Unique key violation or invalid foreign key value: pk=" + ctx.getId());
            }
            else
            {
               throw new CreateException("Failed to create instance: pk=" + ctx.getId() +
                  ", state=" + e.getSQLState() +
                  ", msg=" + e.getMessage());
            }
         }
         pk = ctx.getId();
      }
      return pk;
   }
}
