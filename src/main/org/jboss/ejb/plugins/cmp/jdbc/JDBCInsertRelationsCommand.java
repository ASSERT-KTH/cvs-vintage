/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Iterator;
import javax.ejb.EJBException;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * Inserts relations into a relation table.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.6 $
 */
public class JDBCInsertRelationsCommand {
   protected JDBCStoreManager manager;
   protected Logger log;
    
   // Command name, used for debug trace
   protected String name;

   protected JDBCEntityBridge entity;
   
   public JDBCInsertRelationsCommand(JDBCStoreManager manager) {
      this.manager = manager;
      this.name = "InsertRelations";
      this.entity = manager.getEntityBridge();

      this.log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }
   
   public void execute(RelationData relationData) {
      if(relationData.addedRelations.size() == 0) {
         return;
      }
      
      Connection con = null;
      PreparedStatement ps = null;
      
      try {
         // get the connection
         con = manager.getConnection();
         
         // get the sql
         String theSQL = getSQL(relationData);
         log.debug(name + " command executing: " + theSQL);
         
         // get a prepared statement
         ps = con.prepareStatement(theSQL);
         
         Iterator pairs = relationData.addedRelations.iterator();
         while(pairs.hasNext()) {
            RelationPair pair = (RelationPair)pairs.next();
            
            // set the parameters
            setParameters(ps, relationData, pair);
         
            int rowsAffected = ps.executeUpdate();
         
            log.debug("Rows affected = " + rowsAffected);
         }
      } catch(Exception e) {
         throw new EJBException("Could insert relations into " + relationData.getLeftCMRField().getRelationTableName(), e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }
   
   protected String getSQL(RelationData relationData) throws Exception {
      StringBuffer sql = new StringBuffer();
      sql.append("INSERT INTO ").append(relationData.getLeftCMRField().getRelationTableName());      

      sql.append(" (");
            sql.append(SQLUtil.getColumnNamesClause(relationData.getLeftCMRField().getTableKeyFields()));
            sql.append(", ");
            sql.append(SQLUtil.getColumnNamesClause(relationData.getRightCMRField().getTableKeyFields()));
      sql.append(")");

      sql.append(" VALUES (");
            sql.append(SQLUtil.getValuesClause(relationData.getLeftCMRField().getTableKeyFields()));
            sql.append(", ");
            sql.append(SQLUtil.getValuesClause(relationData.getRightCMRField().getTableKeyFields()));
      sql.append(")");      
      return sql.toString();
   }
      
   protected void setParameters(PreparedStatement ps, RelationData relationData, RelationPair pair) throws Exception {
      int parameterIndex = 1;

      // left keys
      Object leftId = pair.getLeftId();
      JDBCCMPFieldBridge[] leftKeyFields = relationData.getLeftCMRField().getTableKeyFields();
      for(int i=0; i<leftKeyFields.length; i++) {
         parameterIndex = leftKeyFields[i].setPrimaryKeyParameters(ps, parameterIndex, leftId);
      }
            
      // right keys
      Object rightId = pair.getRightId();
      JDBCCMPFieldBridge[] rightKeyFields = relationData.getRightCMRField().getTableKeyFields();
      for(int i=0; i<rightKeyFields.length; i++) {
         parameterIndex = rightKeyFields[i].setPrimaryKeyParameters(ps, parameterIndex, rightId);
      }
   }
}
