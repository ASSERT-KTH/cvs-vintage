/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCEntityBridge2;
import org.jboss.ejb.plugins.cmp.jdbc2.bridge.JDBCCMPFieldBridge2;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityCommandMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.3 $</tt>
 */
public class PkSqlCreateCommand
   implements CreateCommand
{
   private Logger log;
   private JDBCEntityBridge2 entityBridge;
   private String pkSql;
   private JDBCCMPFieldBridge2 pkField;

   public void init(JDBCStoreManager2 manager) throws DeploymentException
   {
      this.entityBridge = (JDBCEntityBridge2) manager.getEntityBridge();
      log = Logger.getLogger(getClass().getName() + "." + entityBridge.getEntityName());

      final JDBCFieldBridge[] pkFields = entityBridge.getPrimaryKeyFields();
      if(pkFields.length > 1)
      {
         throw new DeploymentException("This entity-command cannot be used with composite primary keys!");
      }
      this.pkField = (JDBCCMPFieldBridge2) pkFields[0];

      JDBCEntityCommandMetaData metadata = entityBridge.getMetaData().getEntityCommand();
      pkSql = metadata.getAttribute("pk-sql");
      if(pkSql == null)
      {
         throw new DeploymentException("pk-sql attribute must be set for entity " + entityBridge.getEntityName());
      }
      log.debug("entity-command generate pk sql: " + pkSql);
   }

   public Object execute(Method m, Object[] args, EntityEnterpriseContext ctx) throws CreateException
   {
      Object pk;
      PersistentContext pctx = (PersistentContext) ctx.getPersistenceContext();
      if(ctx.getId() == null)
      {
         Connection con = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try
         {
            log.debug("executing sql: " + pkSql);

            con = entityBridge.getDataSource().getConnection();
            ps = con.prepareStatement(pkSql);
            rs = ps.executeQuery();

            if(!rs.next())
            {
               throw new CreateException("pk-sql " + pkSql + " returned no results!");
            }

            pk = pkField.loadArgumentResults(rs, 1);
            pctx.setFieldValue(pkField.getRowIndex(), pk);
         }
         catch(SQLException e)
         {
            log.error("Failed to execute pk sql. error code: " + e.getErrorCode() + ", sql state: " + e.getSQLState(), e);
            throw new CreateException("Failed to execute pk sql: " + e.getMessage() +
               ", error code: " + e.getErrorCode() + ", sql state: " + e.getSQLState());
         }
         finally
         {
            JDBCUtil.safeClose(rs);
            JDBCUtil.safeClose(ps);
            JDBCUtil.safeClose(con);
         }

         if(pk == null)
         {
            log.error("Primary key for created instance is null.");
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
               throw new CreateException("Failed to create instance: pk=" +
                  ctx.getId() +
                  ", state=" +
                  e.getSQLState() +
                  ", msg=" + e.getMessage());
            }
         }
         pk = ctx.getId();
      }
      return pk;
   }
}
