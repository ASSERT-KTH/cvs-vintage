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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.logging.Logger;

/**
 * JDBCLoadEntityCommand loads the data for an instance from the table.
 * This command implements specified eager loading. For CMP 2.x, the
 * entity can be configured to only load some of the fields, which is 
 * helpful for entitys with lots of data.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.24 $
 */
public class JDBCLoadEntityCommand {
   private final JDBCStoreManager manager;
   private final JDBCEntityBridge entity;
   private final Logger log;

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
      execute(null, ctx);
   }

   public void execute(
         JDBCCMPFieldBridge requiredField, 
         EntityEnterpriseContext ctx) {

      // load the instance primary key fields into the context
      entity.injectPrimaryKeyIntoInstance(ctx, ctx.getId());

      // get the read ahead cache
      PrefetchCache prefetchCache = manager.getPrefetchCache();

      // load any prefetched data into the context context
      prefetchCache.loadPrefetchData(ctx);
      
      // determine the fields to load
      List loadFields = getLoadFields(
            requiredField, 
            JDBCContext.getReadAheadMetaData(ctx),
            ctx);

      // if no there are not load fields return
      if(loadFields.size() == 0) {
         return;
      }

      // get the keys to load
      List loadKeys = JDBCContext.getLoadKeys(ctx);
      if(loadKeys == null)
      {
         loadKeys = Collections.singletonList(ctx.getId());
      }

      // generate the sql
      String sql = getSQL(loadFields, loadKeys.size());
      
      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         con = entity.getDataSource().getConnection();
         
         // create the statement
         if(log.isDebugEnabled()) {
            log.debug("Executing SQL: " + sql);
         }
         ps = con.prepareStatement(sql);

         // Set the fetch size of the statement
         if(manager.getEntityBridge().getFetchSize() > 0) {
            ps.setFetchSize(manager.getEntityBridge().getFetchSize());
         }
         
         // set the parameters
         
         int paramIndex = 1;
         for(Iterator iter = loadKeys.iterator(); iter.hasNext();) {
            paramIndex = entity.setPrimaryKeyParameters(
                  ps, paramIndex, iter.next());
         }

         // execute statement
         ResultSet rs = ps.executeQuery();

         // load results
         boolean mainEntityLoaded = false;
         Object[] ref = new Object[1];
         while(rs.next()) {
            // reset the column index for this row
            int index = 1;

            // ref must be reset to null before load
            ref[0] = null;
   
            // if we are loading more then one entity, load the pk from the row
            Object pk = null;
            if(loadKeys.size() > 1) {
               // load the pk
               index = entity.loadPrimaryKeyResults(rs, index, ref);
               pk = ref[0];
            }

            // is this the main entity or a preload entity
            if(loadKeys.size()==1 || pk.equals(ctx.getId())) {
               // main entity; load the values into the context
               for(Iterator iter = loadFields.iterator(); iter.hasNext();) {
                  JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
                  index = field.loadInstanceResults(rs, index, ctx);
                  field.setClean(ctx);
               }
               mainEntityLoaded = true;
            } else {
               // preload entity; load the values into the read ahead cahce
               for(Iterator iter = loadFields.iterator(); iter.hasNext();) {
                  JDBCFieldBridge field = (JDBCFieldBridge)iter.next();

                  // ref must be reset to null before load
                  ref[0] = null;
   
                  // load the result of the field
                  index = field.loadArgumentResults(rs, index, ref);
   
                  // cache the field value
                  prefetchCache.addPrefetchData(pk, field, ref[0]);
               }
            }
         }
      
         // did we load the main results
         if(!mainEntityLoaded) {
            throw new NoSuchEntityException("Entity not found: primaryKey=" +
                  ctx.getId());
         }
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Load failed", e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   private String getSQL(List loadFields, int keyCount) {
      //
      // column names clause
      StringBuffer columnNamesClause = new StringBuffer();
      // if we are loading more then one entity we need to add the primry
      // key to the load fields to match up the results with the correct
      // entity.
      if(keyCount > 1) {
         columnNamesClause.append(SQLUtil.getColumnNamesClause(
                  entity.getPrimaryKeyFields()));
         columnNamesClause.append(",");
      }
      columnNamesClause.append(SQLUtil.getColumnNamesClause(loadFields));

      //
      // table name clause
      String tableName = entity.getTableName();

      //
      // where clause
      String pkWhere = SQLUtil.getWhereClause(entity.getPrimaryKeyFields());
      StringBuffer whereClause = new StringBuffer(
            (pkWhere.length() + 6) * keyCount + 4);
      for(int i=0; i<keyCount; i++) {
         if(i > 0) {
            whereClause.append(" OR ");
         }
         whereClause.append("(").append(pkWhere).append(")");
      }

      //
      // assemble pieces into final statement
      if(entity.getMetaData().hasRowLocking()) {
         JDBCFunctionMappingMetaData rowLocking = 
               manager.getMetaData().getTypeMapping().getRowLockingTemplate();
         if(rowLocking == null) {
            throw new IllegalStateException("row-locking is not allowed for " +
                  "this type of datastore");
         } else {
            String[] args = new String[] {
               columnNamesClause.toString(), 
               tableName, 
               whereClause.toString()};
            return rowLocking.getFunctionSql(args);
         }
      } else {
         StringBuffer sql = new StringBuffer(
               7 + columnNamesClause.length() +
               6 + tableName.length() +
               7 + whereClause.length());

         sql.append("SELECT ").append(columnNamesClause);
         sql.append(" FROM ").append(tableName);
         sql.append(" WHERE ").append(whereClause);
         return sql.toString();
      }
 
   }

   private List getLoadFields(
         JDBCCMPFieldBridge requiredField,
         JDBCReadAheadMetaData readahead,
         EntityEnterpriseContext ctx) {

      // get the load fields
      ArrayList loadFields = new ArrayList(entity.getFields().size());
      if(requiredField == null) {

         if(readahead != null && !readahead.isNone()) {
            if(log.isTraceEnabled()) {
               log.trace("Eager-load for entity: readahead=" +  readahead);
            }
            loadFields.addAll(
                  entity.getLoadGroup(readahead.getEagerLoadGroup()));
         } else {
            if(log.isTraceEnabled()) {
               log.trace("Default eager-load for entity: readahead=" +
                     readahead);
            }
            loadFields.addAll(entity.getEagerLoadFields());
         }
      } else {
         loadFields.add(requiredField);
         for(Iterator groups = entity.getLazyLoadGroups(); groups.hasNext();) {
            List group = (List)groups.next();
            if(group.contains(requiredField)) {
               for(Iterator fields = group.iterator(); fields.hasNext();) {
                  JDBCFieldBridge field = (JDBCFieldBridge)fields.next();
                  if(!loadFields.contains(field)) {
                     loadFields.add(field);
                  }
               }
            }
         }
      }

      // remove any field that is a member of the primary key
      // or has not timed out or is already loaded
      for(Iterator fields = loadFields.iterator(); fields.hasNext();) {
         JDBCFieldBridge field = (JDBCFieldBridge)fields.next();

         // the field removed from loadFields if:
         // - it is a primary key member (should already be loaded)
         // - it is already loaded
         // - it is a read-only _already_loaded_ field that isn't timed out yet
         // - it is a CMR field with a foreign key mapped to a pk
         if(field.isPrimaryKeyMember()
            || field.isLoaded( ctx )
               && (!field.isReadOnly() || !field.isReadTimedOut(ctx))
            || field instanceof JDBCCMRFieldBridge
               && ((JDBCCMRFieldBridge)field).isFkPartOfPk()) {
            fields.remove();
         }
      }
      return loadFields;
   }
}
