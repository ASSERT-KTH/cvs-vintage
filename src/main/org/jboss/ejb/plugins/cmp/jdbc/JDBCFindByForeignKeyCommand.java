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
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * Finds the IDs of related entities by a foreign key in the related
 * entity's table.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.8 $
 */
public class JDBCFindByForeignKeyCommand {
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private Logger log;
   
   public JDBCFindByForeignKeyCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   } 

   public Set execute(Object foreignKey,
         JDBCCMPFieldBridge[] foreignKeyFields) {
            
      // generate SQL
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(
               entity.getJDBCPrimaryKeyFields()));
      sql.append(" FROM ").append(entity.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(foreignKeyFields));

      if(entity.getMetaData().hasSelectForUpdate()) {
         sql.append(" FOR UPDATE");
      }
      
      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         con = manager.getDataSource().getConnection();
         
         // create the statement
         ps = con.prepareStatement(sql.toString());
         
         // set the parameters
         int index = 1;
         for(int i=0; i<foreignKeyFields.length; i++) {
            index = foreignKeyFields[i].setPrimaryKeyParameters(
                  ps, index, foreignKey);
         }

         // execute statement
         ResultSet rs = ps.executeQuery();

         // load the results
         Set result = new HashSet();   
         Object[] pkRef = new Object[1];
         while(rs.next()) {
            pkRef[0] = null;   
            entity.loadPrimaryKeyResults(rs, 1, pkRef);
            result.add(pkRef[0]);
         }

         // success
         return result;
      } catch(Exception e) {
         throw new EJBException("Find by foreign-key failed", e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }
}
