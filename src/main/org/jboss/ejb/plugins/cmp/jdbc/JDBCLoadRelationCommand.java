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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator; 
import java.util.HashMap; 
import java.util.List; 
import java.util.Map; 
import javax.ejb.EJBException; 

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.logging.Logger;

/**
 * Loads relations for a particular entity from a relation table.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.24 $
 */
public class JDBCLoadRelationCommand {
   private final JDBCStoreManager manager;
   private final JDBCEntityBridge entity;
   private final Logger log;

   public JDBCLoadRelationCommand(JDBCStoreManager manager) {
      this.manager = manager;
      this.entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }

   public Collection execute(JDBCCMRFieldBridge cmrField, Object pk) {
      JDBCCMRFieldBridge relatedCMRField = cmrField.getRelatedCMRField();

      // get the read ahead cahces
      ReadAheadCache readAheadCache = manager.getReadAheadCache();
      ReadAheadCache relatedReadAheadCache = cmrField.getRelatedManager().getReadAheadCache();
      
      // get the finder results associated with this context, if it exists
      ReadAheadCache.EntityReadAheadInfo info = readAheadCache.getEntityReadAheadInfo(pk);
      List loadKeys = info.getLoadKeys();

      // generate the sql
      String sql = getSQL(cmrField, loadKeys.size());
  
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try {
         // create the statement
         if(log.isDebugEnabled())
            log.debug("Executing SQL: " + sql);

         // get the connection
         con = cmrField.getDataSource().getConnection();
         ps = con.prepareStatement(sql.toString());

         // Set the fetch size of the statement
         if(manager.getEntityBridge().getFetchSize() > 0) {
            ps.setFetchSize(manager.getEntityBridge().getFetchSize());
         }
         
         // get the load fields
         List myKeyFields = getMyKeyFields(cmrField);
         List relatedKeyFields = getRelatedKeyFields(cmrField);
         List preloadFields = getPreloadFields(cmrField);
         
         // set the parameters
         int paramIndex = 1;
         for(int i = 0; i < loadKeys.size(); i++) {
            Object key = loadKeys.get(i);
            for(int j = 0; j < myKeyFields.size(); ++j) {
               JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)myKeyFields.get(j);
               paramIndex = field.setPrimaryKeyParameters(ps, paramIndex, key);
            }
         }

         // execute statement
         rs = ps.executeQuery();

         // initialize the results map
         Map resultsMap = new HashMap(loadKeys.size());
         for(int i = 0; i < loadKeys.size(); ++i) {
            resultsMap.put(loadKeys.get(i), new ArrayList());
         }

         // load the results
         Object[] ref = new Object[1];
         while(rs.next()) {
            // reset the column index for this row
            int index = 1;

            // ref must be reset to null before each load
            ref[0] = null;

            // if we are loading more then one entity, load the pk from the row
            Object loadedPk = pk;
            if(loadKeys.size() > 1) {
               // load the pk
               for(int i = 0; i < myKeyFields.size(); ++i) {
                  JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)myKeyFields.get(i);
                  index = field.loadPrimaryKeyResults(rs, index, ref);
               }      
               loadedPk = ref[0];
            }
 
            // load the fk
            ref[0] = null;
            for(int i = 0; i < relatedKeyFields.size(); ++i) {
               JDBCCMPFieldBridge field = (JDBCCMPFieldBridge)relatedKeyFields.get(i);
               index = field.loadPrimaryKeyResults(rs, index, ref);
            }      
            Object loadedFk = ref[0];
   
            if(loadedFk != null) {
               // add this value to the list for loadedPk
               List results = (List)resultsMap.get(loadedPk);
               results.add(loadedFk);

               // if the related cmr field is single valued we can pre-load
               // the reverse relationship
               if(relatedCMRField.isSingleValued()) {
                  relatedReadAheadCache.addPreloadData(
                        loadedFk,
                        relatedCMRField,
                        Collections.singletonList(loadedPk));
               }

               // read the preload fields
               for(int i = 0; i < preloadFields.size(); ++i) {
                  JDBCFieldBridge field = (JDBCFieldBridge)preloadFields.get(i);
                  ref[0] = null;

                  // read the value and store it in the readahead cache
                  index = field.loadArgumentResults(rs, index, ref);
                  relatedReadAheadCache.addPreloadData(loadedFk, field, ref[0]);
               }
            }
         }

         // set all of the preloaded values
         for(Iterator iter=resultsMap.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();

            // get the results for this key
            List results = (List)resultsMap.get(key);

            // store the results list for readahead on-load
            JDBCReadAheadMetaData readAhead = cmrField.getReadAhead();
            relatedReadAheadCache.addFinderResults(results, readAhead);

            // store the preloaded relationship (unless this is the realts we
            // are actually after)
            if(!key.equals(pk)) {
               readAheadCache.addPreloadData(key, cmrField, results);
            }
         }

         // success, return the results
         return (List)resultsMap.get(pk); 
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Load relation failed", e);
      } finally {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   private String getSQL(JDBCCMRFieldBridge cmrField, int keyCount) {

      List myKeyFields = getMyKeyFields(cmrField);
      List relatedKeyFields = getRelatedKeyFields(cmrField);
      List preloadFields = getPreloadFields(cmrField);
      String relationTable = getRelationTable(cmrField);
      String relatedTable = cmrField.getRelatedJDBCEntity().getTableName();
      
      // do we need to join the relation table and the related table
      // do we need to join the relation table and the related table
      boolean join = ((preloadFields.size() > 0) || cmrField.allFkFieldsMappedToPkFields())
         && !relationTable.equals(relatedTable);

      // aliases for the tables, only required if we are joining the tables
      String relationTableAlias = SQLUtil.EMPTY_STRING;
      String relatedTableAlias = SQLUtil.EMPTY_STRING;
      if(join) {
         relationTableAlias = relationTable;
         relatedTableAlias = relatedTable;
      }

      //
      // column names clause
      // 
      StringBuffer columnNamesClause = new StringBuffer(100);
      if(keyCount > 1) {
         columnNamesClause.append(SQLUtil.getColumnNamesClause(myKeyFields, relationTableAlias))
            .append(SQLUtil.COMMA);
      }
      columnNamesClause.append(SQLUtil.getColumnNamesClause(relatedKeyFields, relationTableAlias));
      if(preloadFields.size() > 0) {
         columnNamesClause.append(SQLUtil.COMMA).
            append(SQLUtil.getColumnNamesClause(preloadFields, relatedTableAlias));
      }

      //
      // from clause
      //
      StringBuffer fromClause = new StringBuffer(100);
      fromClause.append(relationTable);
      if(join) {
         fromClause.append(SQLUtil.COMMA).append(relatedTable);
      }

      //
      // where clause
      // 
      StringBuffer whereClause = new StringBuffer(150);
      // add the join 
      if(join) {
         // join the tables
         whereClause
            .append('(')
            .append(
               SQLUtil.getJoinClause(
                  relatedKeyFields,
                  relationTable,
                  cmrField.getRelatedJDBCEntity().getPrimaryKeyFields(),
                  relatedTable))
            .append(')').append(SQLUtil.AND).append('(');
      }

      // add the keys
      String pkWhere = SQLUtil.getWhereClause(myKeyFields, relationTableAlias);
      for(int i=0; i<keyCount; i++) {
         if(i > 0) {
            whereClause.append(SQLUtil.OR);
         }
         whereClause.append('(').append(pkWhere).append(')');
      }
      if(join) {
         whereClause.append(')');
      }
      
      //
      // assemble pieces into final statement
      //
      JDBCFunctionMappingMetaData selectTemplate = getSelectTemplate(cmrField);
      if(selectTemplate != null) {
         String[] args = new String[] {
               columnNamesClause.toString(),
               fromClause.toString(),
               whereClause.toString()};
         return selectTemplate.getFunctionSql(args);
      } else {
         StringBuffer sql = new StringBuffer(
               7 + columnNamesClause.length() +
               6 + fromClause.length() +
               7 + whereClause.length());

         sql.append(SQLUtil.SELECT).append(columnNamesClause.toString())
            .append(SQLUtil.FROM).append(fromClause.toString())
            .append(SQLUtil.WHERE).append(whereClause.toString());
         return sql.toString();
      }
   }

   private List getMyKeyFields(JDBCCMRFieldBridge cmrField) {
      if(cmrField.getRelationMetaData().isTableMappingStyle()) {
         // relation table
         return cmrField.getTableKeyFields();
      } else if(cmrField.getRelatedCMRField().hasForeignKey()) {
         // related has foreign key
         return cmrField.getRelatedCMRField().getForeignKeyFields();
      } else {
         // i have foreign key
         return entity.getPrimaryKeyFields();
      }
   }
 
   private List getRelatedKeyFields(JDBCCMRFieldBridge cmrField) {
      if(cmrField.getRelationMetaData().isTableMappingStyle()) {
         // relation table
         return cmrField.getRelatedCMRField().getTableKeyFields();
      } else if(cmrField.getRelatedCMRField().hasForeignKey()) {
         // related has foreign key
         return cmrField.getRelatedJDBCEntity().getPrimaryKeyFields();
      } else {
         // i have foreign key
         return cmrField.getForeignKeyFields();
      }
   } 

   private List getPreloadFields(JDBCCMRFieldBridge cmrField) {
      if(!cmrField.getReadAhead().isOnFind()) {
         return Collections.EMPTY_LIST;
      }

      JDBCCMRFieldBridge relatedCMRField = cmrField.getRelatedCMRField();
      String eagerLoadGroup = cmrField.getReadAhead().getEagerLoadGroup();
      List eagerLoad = relatedCMRField.getEntity().getLoadGroup(eagerLoadGroup);
      
      // add all the eagerload fields except for the related cmr field
      List preloadFields = new ArrayList(eagerLoad.size());
      for(int i = 0; i < eagerLoad.size(); ++i) {
         JDBCFieldBridge field = (JDBCFieldBridge)eagerLoad.get(i);
         if(!field.equals(relatedCMRField)) {
            preloadFields.add(field);
         }
      }
      return Collections.unmodifiableList(preloadFields);
   }

   private String getRelationTable(JDBCCMRFieldBridge cmrField) {
      if(cmrField.getRelationMetaData().isTableMappingStyle()) {
         // relation table
         return cmrField.getTableName();
      } else if(cmrField.getRelatedCMRField().hasForeignKey()) {
         // related has foreign key
         return cmrField.getRelatedJDBCEntity().getTableName();
      } else {
         // i have foreign key
         return entity.getTableName();
      }
   }

   private JDBCFunctionMappingMetaData getSelectTemplate(JDBCCMRFieldBridge cmrField) {

      JDBCFunctionMappingMetaData selectTemplate = null;
      if(cmrField.getRelationMetaData().isTableMappingStyle()) {
         // relation table
         if(cmrField.getRelationMetaData().hasRowLocking()) {
            selectTemplate =
               cmrField.getRelationMetaData().getTypeMapping().getRowLockingTemplate();
            if(selectTemplate == null) {
               throw new IllegalStateException(
                  "row-locking is not allowed for this type of datastore");
            }
         }
      } else if(cmrField.getRelatedCMRField().hasForeignKey()) {
         // related has foreign key
         if(cmrField.getRelatedJDBCEntity().getMetaData().hasRowLocking()) {
            selectTemplate =
               cmrField.getRelatedJDBCEntity().getMetaData().getTypeMapping().getRowLockingTemplate();
            if(selectTemplate == null) {
               throw new IllegalStateException(
                  "row-locking is not allowed for this type of datastore");
            }
         }
      } else {
         // i have foreign key
         if(entity.getMetaData().hasRowLocking()) {
            selectTemplate = entity.getMetaData().getTypeMapping().getRowLockingTemplate();
            if(selectTemplate == null) {
               throw new IllegalStateException(
                  "row-locking is not allowed for this type of datastore");
            }
         }
      }
      return selectTemplate;
   }
}
