/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.logging.Logger;

/**
 * JDBCLoadEntityCommand loads the data for an instance from the table.
 * This command implements specified eager loading. For CMP 2.x, the
 * entity can be configured to only load some of the fields, which is 
 * helpful for entitys with lots of data.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.13 $
 */
public class JDBCLoadEntityCommand {
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private Logger log;

   public JDBCLoadEntityCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }

   public void execute(EntityEnterpriseContext ctx) {
      // load the instance primary key fields
      entity.injectPrimaryKeyIntoInstance(ctx, ctx.getId());
      
      // determine the fields to load
      JDBCCMPFieldBridge[] loadFields = getLoadFields(ctx);

      // if no there are not load fields return
      if(loadFields.length == 0) {
         return;
      }

      // generate the sql
      StringBuffer sql = new StringBuffer();
      String columnNamesClause = SQLUtil.getColumnNamesClause(loadFields);
      String tableName = entity.getTableName();
      String whereClause = SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields());

      if (entity.getMetaData().hasRowLocking())
      {
         JDBCFunctionMappingMetaData rowLocking = manager.getMetaData().getTypeMapping().getRowLockingTemplate();
         if (rowLocking == null)
         {
            throw new IllegalStateException("row-locking is not allowed for this type of datastore");
         }
         else
         {
            String[] args = new String[] {columnNamesClause, tableName, whereClause};
            sql.append(rowLocking.getFunctionSql(args));
         }
      }
      else
      {
         sql.append("SELECT ").append(columnNamesClause);
         sql.append(" FROM ").append(tableName);
         sql.append(" WHERE ").append(whereClause);
      }
      
      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         con = entity.getDataSource().getConnection();
         
         // create the statement
         if (log.isDebugEnabled())
            log.debug("Executing SQL: " + sql);
         ps = con.prepareStatement(sql.toString());
         
         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, ctx.getId());

         // execute statement
         ResultSet rs = ps.executeQuery();

         // did we get results
         if(!rs.next()) {
            throw new NoSuchEntityException("Entity " + ctx.getId() + 
                  " not found");
         }
      
         // load each field and mark it clean
         int index = 1;
         for(int i=0; i<loadFields.length; i++) {
            index = loadFields[i].loadInstanceResults(rs, index, ctx);
            loadFields[i].setClean(ctx);
         }
      
         // mark the entity as created; if it was loaded it was created 
         entity.setCreated(ctx);
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Load failed", e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   private JDBCCMPFieldBridge[] getLoadFields(EntityEnterpriseContext ctx) {
      JDBCCMPFieldBridge[] eagerFields = entity.getEagerLoadFields();
      ArrayList fields = new ArrayList(eagerFields.length);

      for(int i=0; i<eagerFields.length; i++) {
         if(!eagerFields[i].isPrimaryKeyMember() &&
            eagerFields[i].isReadTimedOut(ctx)) {
            fields.add(eagerFields[i]);
         }
      }
      return (JDBCCMPFieldBridge[])fields.toArray(
            new JDBCCMPFieldBridge[fields.size()]);
   }
}
