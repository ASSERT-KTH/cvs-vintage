/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;


import java.util.List;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.ejb.EJBException;

import org.jboss.util.FinderResults;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCReadAheadCommand
 *
 * @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
 * @version $Revision: 1.4 $
 */
public class JDBCReadAheadCommand {

   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private Logger log;
   
   private JDBCCMPFieldBridge[] lastLoadFields;
   private String lastFullClause;
   private int lastKeysCount = -1;

   public JDBCReadAheadCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }

   public void execute(
         JDBCCMPFieldBridge[] loadFields, 
         FinderResults finderResults, 
         int from, 
         int to) {

      // is there anything to load?
      if((loadFields.length == 0) || 
            !(finderResults.getAllKeys() instanceof List)) {

         return;
      }

      // get the keys of the entities we want to load
      Object[] keys = 
            ((List)finderResults.getAllKeys()).subList(from, to).toArray();

      // generate the sql
      String sql = getSQL(loadFields, keys.length);

      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         con = manager.getDataSource().getConnection();
         
         // create the statement
         ps = con.prepareStatement(sql);
         
         // set the parameters
         int index = 1;
         for(int i = 0; i < keys.length; i++) {
            index = entity.setPrimaryKeyParameters(ps, index, keys[i]);
         }

         // execute statement
         ResultSet rs = ps.executeQuery();

         // did we get results
         Object[] ref = new Object[1];
         while(rs.next()) {
            // reset the prameter index for this row
            index = 1;
           
            // ref must be reset to null 
            ref[0] = null;
            
            // load the pk
            index = entity.loadPrimaryKeyResults(rs, index, ref);
            Object pk = ref[0];

            // load each field and store it in the cache
            for(int i = 0; i < loadFields.length; i++) {
               // ref must be reset to null
               ref[0] = null;

               // load the result of the field
               index = loadFields[i].loadArgumentResults(rs, index, ref);

               // cache the field value
               manager.addPreloadData(pk, loadFields[i], ref[0]);
            }
         }
      } catch(Exception e) {
         throw new EJBException("Load failed", e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   private String getSQL(JDBCCMPFieldBridge[] loadFields, int keysCount) {

      // We can reuse the last generated sql if the load fields have
      // not chanded and the key count.
      boolean canUseLast = (keysCount == lastKeysCount) && 
            (loadFields.length == lastLoadFields.length);

      if(canUseLast) {
         for(int i = 0; i < loadFields.length; i++) {
            if(!loadFields[i].getMetaData().equals(
                     lastLoadFields[i].getMetaData())) {

               canUseLast = false;
               break;
            }
         }
      }

      if(!canUseLast) {
         // SELECT pkFields, loadFields
         // FROM table 
         // WHERE pk1=? AND pk2=? OR pk1=? AND pk2=? ...

         StringBuffer sql = new StringBuffer(1024);

         sql.append("SELECT ");
            sql.append(SQLUtil.getColumnNamesClause(
                     entity.getJDBCPrimaryKeyFields()));
            sql.append(",");
            sql.append(SQLUtil.getColumnNamesClause(loadFields));
            
         sql.append(" FROM ").append(entity.getTableName());

         sql.append(" WHERE ");
         for(int i=0; i < keysCount; i++) {
            if(i > 0) {
               sql.append(" OR ");
            }
            sql.append(SQLUtil.getWhereClause(
                     entity.getJDBCPrimaryKeyFields()));
         }
         
         lastKeysCount = keysCount;
         lastLoadFields = loadFields;
         lastFullClause = sql.toString();
      }
      return lastFullClause;
   }
}
