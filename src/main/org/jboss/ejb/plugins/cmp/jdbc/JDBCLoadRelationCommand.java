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
import java.util.HashSet; 
import java.util.Set; 
import javax.ejb.EJBException; 

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.logging.Logger;

/**
 * Loads relations for a particular entity from a relation table.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.10 $
 */
public class JDBCLoadRelationCommand {
   private JDBCStoreManager manager;
   private Logger log;

   public JDBCLoadRelationCommand(JDBCStoreManager manager) {
      this.manager = manager;

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }

   public Set execute(JDBCCMRFieldBridge cmrField, Object pk) {
      // get the key fields
      JDBCCMPFieldBridge[] myKeyFields = cmrField.getTableKeyFields();
      JDBCCMPFieldBridge[] relatedKeyFields = 
            cmrField.getRelatedCMRField().getTableKeyFields();

      // generate SQL
      StringBuffer sql = new StringBuffer();
      String columnNamesClause = SQLUtil.getColumnNamesClause(cmrField.getRelatedCMRField().getTableKeyFields());
      String tableName = cmrField.getRelationMetaData().getTableName();
      String whereClause = SQLUtil.getWhereClause(cmrField.getTableKeyFields());

      if (cmrField.getRelationMetaData().hasRowLocking())
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
         con = cmrField.getRelationMetaData().getDataSource().getConnection();
         
         // create the statement
         if (log.isDebugEnabled())
            log.debug("Executing SQL: " + sql);
         ps = con.prepareStatement(sql.toString());
         
         // set the parameters
         int index = 1;
         for(int i=0; i<myKeyFields.length; i++) {
            index = myKeyFields[i].setPrimaryKeyParameters(ps, index, pk);
         }

         // execute statement
         ResultSet rs = ps.executeQuery();

         // load the results
         Set result = new HashSet();   
         Object[] pkRef = new Object[1];
         while(rs.next()) {
            pkRef[0] = null;   
            index = 1;
            for(int i=0; i<relatedKeyFields.length; i++) {
               index = relatedKeyFields[i].loadPrimaryKeyResults(
                     rs, index, pkRef);
            }      
            result.add(pkRef[0]);
         }

         // success
         return result;
      } catch(Exception e) {
         throw new EJBException("Load relation by foreign-key failed", e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }
}
