/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Iterator;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.EJBException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.CMPStoreManager;
import org.jboss.ejb.plugins.cmp.LoadEntityCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

/**
 * JDBCLoadEntityCommand loads the data for an instance from the table.
 * This command implements specified eager loading. For CMP 2.x, the
 * entity can be configured to only load some of the fields, which is 
 * helpful for entitys with lots of data.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.6 $
 */
public class JDBCLoadEntityCommand
   extends JDBCQueryCommand
   implements LoadEntityCommand
{
   // Constructors --------------------------------------------------

   public JDBCLoadEntityCommand(JDBCStoreManager manager) {
      super(manager, "Load");
   }

   // LoadEntityCommand implementation ---------------------------

   public void execute(EntityEnterpriseContext ctx) throws RemoteException {
      // load the instance primary key fields
      entity.injectPrimaryKeyIntoInstance(ctx, ctx.getId());
      
      // pass this info on
      ExecutionState es = new ExecutionState();
      es.ctx = ctx;
      es.fields = getLoadFields(ctx);

      if(es.fields.length > 0) {
         try {
            jdbcExecute(es);
         } catch (Exception e) {
            throw new ServerException("Load failed", e);
         }
      }
      
      // mark the entity as created; if it was loaded it was created 
      entity.setCreated(ctx);
   }

   // JDBCQueryCommand overrides ------------------------------------

   protected String getSQL(Object argOrArgs) throws Exception {
      ExecutionState es = (ExecutionState)argOrArgs;

      
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(es.fields));
      sql.append(" FROM ").append(entityMetaData.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));
      
      return sql.toString();
   }

   protected void setParameters(PreparedStatement ps, Object arg)
      throws Exception
   {
      ExecutionState es = (ExecutionState)arg;      
      entity.setPrimaryKeyParameters(ps, 1, es.ctx.getId());
   }

   protected Object handleResult(ResultSet rs, Object arg) throws Exception {
      ExecutionState es = (ExecutionState)arg;      

      if(!rs.next()) {
         throw new NoSuchObjectException("Entity " + es.ctx.getId() + " not found");
      }
      
      // load each field
      int parameterIndex = 1;
      for(int i=0; i<es.fields.length; i++) {
         parameterIndex = es.fields[i].loadInstanceResults(rs, parameterIndex, es.ctx);
         es.fields[i].setClean(es.ctx);
      }
      
      return null;
   }
   
   protected JDBCCMPFieldBridge[] getLoadFields(EntityEnterpriseContext ctx) {
      JDBCCMPFieldBridge[] eagerFields = entity.getEagerLoadFields();
      ArrayList fields = new ArrayList(eagerFields.length);

      for(int i=0; i<eagerFields.length; i++) {
         if(!eagerFields[i].isPrimaryKeyMember() &&
            eagerFields[i].isReadTimedOut(ctx)) {
            fields.add(eagerFields[i]);
         }
      }
      return (JDBCCMPFieldBridge[])fields.toArray(new JDBCCMPFieldBridge[fields.size()]);
   }
   
   private class ExecutionState {
      public JDBCCMPFieldBridge[] fields;
      public EntityEnterpriseContext ctx;
   }
}
