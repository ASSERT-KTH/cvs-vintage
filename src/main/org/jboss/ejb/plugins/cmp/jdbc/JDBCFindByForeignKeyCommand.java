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

/**
 * Finds the IDs of related entities by a foreign key in the related
 * entity's table.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.6 $
 */
public class JDBCFindByForeignKeyCommand extends JDBCQueryCommand {
   // Constructors --------------------------------------------------
   
   public JDBCFindByForeignKeyCommand(JDBCStoreManager manager) {
      super(manager, "FindByForeignKey");
   }
   
   // FindEntitiesCommand implementation -------------------------
   
   public Set execute(Object foreignKey,
         JDBCCMPFieldBridge[] foreignKeyFields) {
            
      ExecutionState es = new ExecutionState();
      es.foreignKey = foreignKey;
      es.foreignKeyFields = foreignKeyFields;

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
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(entity.getJDBCPrimaryKeyFields()));
      sql.append(" FROM ").append(entityMetaData.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(es.foreignKeyFields));
      
      return sql.toString();
   }

   protected void setParameters(PreparedStatement ps, Object arg) throws Exception {
      ExecutionState es = (ExecutionState)arg;
      
      int parameterIndex = 1;
      for(int i=0; i<es.foreignKeyFields.length; i++) {
         parameterIndex = es.foreignKeyFields[i].setPrimaryKeyParameters(ps, parameterIndex, es.foreignKey);
      }
   }

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception {   
      Set result = new HashSet();   

      Object[] pkRef = new Object[1];
      while(rs.next()) {
         pkRef[0] = null;
         entity.loadPrimaryKeyResults(rs, 1, pkRef);
         result.add(pkRef[0]);
      }

      return result;
   }

   private static class ExecutionState {
      public Object foreignKey;
      public JDBCCMPFieldBridge[] foreignKeyFields;
   }
}
