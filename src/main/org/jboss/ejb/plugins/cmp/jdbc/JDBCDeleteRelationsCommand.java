/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.PreparedStatement;
import java.util.Iterator;
import javax.ejb.EJBException;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

/**
 * Deletes relations from a relation table.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public class JDBCDeleteRelationsCommand
   extends JDBCUpdateCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCDeleteRelationsCommand(JDBCStoreManager manager) {
		super(manager, "DeleteRelations");
	}
   
	public void execute(RelationData relationData) {
		if(relationData.removedRelations.size() == 0) {
			return;
		}
		
		try {
			jdbcExecute(relationData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new EJBException("Could delete relations from " + relationData.getLeftCMRField().getRelationTableName(), e);
		}
	}
   
	protected String getSQL(Object arg) throws Exception {
		RelationData relationData = (RelationData) arg;
		
		StringBuffer sql = new StringBuffer();
      sql.append("DELETE FROM ").append(relationData.getLeftCMRField().getRelationTableName());
		sql.append(" WHERE ");
		
		Iterator pairs = relationData.removedRelations.iterator();
		while(pairs.hasNext()) {
			RelationPair pair = (RelationPair)pairs.next();
		   sql.append("(");
				// left keys
				sql.append(SQLUtil.getWhereClause(relationData.getLeftCMRField().getTableKeyFields()));
				sql.append(" AND ");
				// right keys
				sql.append(SQLUtil.getWhereClause(relationData.getRightCMRField().getTableKeyFields()));
		   sql.append(")");
		
			if(pairs.hasNext()) {
				sql.append(" OR ");
			} 
		}
		
      return sql.toString();
	}
      
   protected void setParameters(PreparedStatement ps, Object arg) throws Exception {
		RelationData relationData = (RelationData) arg;
		
		int parameterIndex = 1;
		Iterator pairs = relationData.removedRelations.iterator();
		while(pairs.hasNext()) {
			RelationPair pair = (RelationPair)pairs.next();
			
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
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) throws Exception {
		return null;
   }
}
