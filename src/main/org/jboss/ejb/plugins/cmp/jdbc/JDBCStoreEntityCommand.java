/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.Iterator;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;

import javax.ejb.EJBException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.CMPStoreManager;
import org.jboss.ejb.plugins.cmp.StoreEntityCommand;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

/**
 * JDBCStoreEntityCommand updates the row with the new state.
 * This command now always does tuned updates. In the event that
 * no field is dirty the command just returns.  Note: read-only 
 * fields are never considered dirty.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.5 $
 */
public class JDBCStoreEntityCommand
   extends JDBCUpdateCommand
   implements StoreEntityCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCStoreEntityCommand(JDBCStoreManager manager) {
      super(manager, "Store");
   }
   
   // StoreEntityCommand implementation ---------------------------
   
   /**
   * if the readOnly flag is specified in the xml file this won't store.
   * if not a tuned or untuned update is issued.
   */
   public void execute(EntityEnterpriseContext ctx) throws RemoteException {
      // Check for read-only
      // JF: Shouldn't this throw an exception?
      if (entityMetaData.isReadOnly()) {
         return;
      }
      
      try {
         ExecutionState es = new ExecutionState();
         es.ctx = ctx;
         es.fields = (JDBCCMPFieldBridge[])entity.getDirtyFields(ctx);
         
         if(es.fields.length > 0) {
            jdbcExecute(es);         
         } else {
            log.debug(name + " command NOT executed bean is not dirty: id=" + 
                  ctx.getId());
         }
      } catch (Exception e) {
         e.printStackTrace();
         throw new ServerException("Store failed", e);
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   /**
    * Returns dynamically-generated SQL if this entity
    * has tuned updates, otherwise static SQL.
    */
   protected String getSQL(Object argOrArgs) throws Exception {
      ExecutionState es = (ExecutionState)argOrArgs;

      StringBuffer sql = new StringBuffer(); 
      sql.append("UPDATE ").append(entityMetaData.getTableName());
      sql.append(" SET ").append(SQLUtil.getSetClause(es.fields));
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));
      return sql.toString();
   }
   
   protected void setParameters(PreparedStatement ps, Object arg) throws Exception {
      ExecutionState es = (ExecutionState)arg;
      
      int index = 1;
      index = entity.setInstanceParameters(ps, index, es.ctx, es.fields);
      index = entity.setPrimaryKeyParameters(ps, index, es.ctx.getId());
   }
   
   protected Object handleResult(int rowsAffected, Object arg) throws Exception {
      ExecutionState es = (ExecutionState)arg;      

      if(rowsAffected != 1) {
         throw new EJBException("Update of " + entity.getEntityName() + " EJB failed id=" + es.ctx.getId() + " rowsAffected=" + rowsAffected);
      }

      for(int i=0; i<es.fields.length; i++) {
         es.fields[i].setClean(es.ctx);
      }

      return null;
   }
   
   // Protected -----------------------------------------------------
   
   // Inner Classes -------------------------------------------------
   
   protected static class ExecutionState {
      public EntityEnterpriseContext ctx;
      public JDBCCMPFieldBridge[] fields;
   }
}
