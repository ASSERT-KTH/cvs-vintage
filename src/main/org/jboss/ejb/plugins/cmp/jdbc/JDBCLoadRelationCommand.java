/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.PreparedStatement; 
import java.sql.ResultSet; 
import java.util.HashSet; 
import java.util.Set; 
import javax.ejb.EJBException; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge; 

/**
 * Loads relations for a particular entity from a relation table.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.5 $
 */
public class JDBCLoadRelationCommand extends JDBCQueryCommand {
   // Constructors --------------------------------------------------
   
   public JDBCLoadRelationCommand(JDBCStoreManager manager) {
      super(manager, "LoadRelation");
   }
   
   // FindEntitiesCommand implementation -------------------------
   
   public Set execute(JDBCCMRFieldBridge cmrField, Object pk) {
            
      ExecutionState es = new ExecutionState();
      es.cmrField = cmrField;
      es.pk = pk;

      try {
         return (Set)jdbcExecute(es);
      } catch (Exception e) {
         throw new EJBException("FindByForeignKey failed", e);
      }
   }

   protected String getSQL(Object arg) throws Exception {
      ExecutionState es = (ExecutionState)arg;

      // Create table SQL
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(es.cmrField.getRelatedCMRField().getTableKeyFields()));
      sql.append(" FROM ").append(es.cmrField.getRelationTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(es.cmrField.getTableKeyFields()));
      
      return sql.toString();
   }

   protected void setParameters(PreparedStatement ps, Object arg) throws Exception {
      ExecutionState es = (ExecutionState)arg;
      
      JDBCCMPFieldBridge[] myKeyFields = es.cmrField.getTableKeyFields();
      
      int parameterIndex = 1;
      for(int i=0; i<myKeyFields.length; i++) {
         parameterIndex = myKeyFields[i].setPrimaryKeyParameters(ps, parameterIndex, es.pk);
      }
   }

   protected Object handleResult(ResultSet rs, Object arg) throws Exception {   
      ExecutionState es = (ExecutionState)arg;

      Set result = new HashSet();   

      Object[] pkRef = new Object[1];
      while(rs.next()) {
         pkRef[0] = null;   
         
         JDBCCMPFieldBridge[] relatedKeyFields = es.cmrField.getRelatedCMRField().getTableKeyFields();

         int parameterIndex = 1;
         for(int i=0; i<relatedKeyFields.length; i++) {
            parameterIndex = relatedKeyFields[i].loadPrimaryKeyResults(rs, parameterIndex, pkRef);
         }      
         result.add(pkRef[0]);
      }

      return result;
   }

   private static class ExecutionState {
      private JDBCCMRFieldBridge cmrField;
      private Object pk;
   }
}
