/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.reflect.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.EJBException;

import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;

import org.jboss.ejb.plugins.cmp.bridge.CMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

import org.jboss.logging.Log;

/**
 * Represents a key field in a relation table. This class wraps
 * the primary key of the entity. As the relation table row does
 * not represent an entity, methods that get and set values of 
 * the instance throw an illegal argument exception.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */                            
public class JDBCRelationKeyField implements JDBCCMPFieldBridge {
	protected JDBCCMP2xFieldBridge cmpField;
	protected JDBCType jdbcType;
	protected Log log;
	
	public JDBCRelationKeyField(JDBCCMP2xFieldBridge cmpField, String baseColumnName, Log log) throws DeploymentException {
		this.cmpField = cmpField;
		this.log = log;
		
		jdbcType = new JDBCTypeForeignKeyWrapper(cmpField.getJDBCType(), baseColumnName);
	}

	public boolean isReadOnly() {
		return false;
	}

	public boolean isReadTimedOut(EntityEnterpriseContext ctx) {
		return true;
	}
	
	public boolean isPrimaryKeyMember() {
		return false;
	}

	public JDBCType getJDBCType() {
		return jdbcType;
	}

	//
	// Unimplemented functions: relation key does not have a value and is only
	// used as a place holder for relation tables. JDBCForeignKeyField impelemts these.
	//
	public Object getInstanceValue(EntityEnterpriseContext ctx) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}
	
   public void setInstanceValue(EntityEnterpriseContext ctx, Object value) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}
	
	public boolean isDirty(EntityEnterpriseContext ctx) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}

	public void setClean(EntityEnterpriseContext ctx) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}

	public void resetPersistenceContext(EntityEnterpriseContext ctx) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}
	
	public void initInstance(EntityEnterpriseContext ctx) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}		

	public int setInstanceParameters(PreparedStatement ps, int parameterIndex, EntityEnterpriseContext ctx) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}	

	public int loadInstanceResults(ResultSet rs, int parameterIndex, EntityEnterpriseContext ctx) {
		throw new IllegalArgumentException("JDBCRelationKeyFields do not have an instance value");
	}		
	
	//
	// Type wrapper: used to change name of relationship columns
	//
	private static class JDBCTypeForeignKeyWrapper implements JDBCType {
		private JDBCType type;
		private String[] columnNames;
		
		public JDBCTypeForeignKeyWrapper(JDBCType type, String baseColumnName) {
			this.type = type;
			
			columnNames = (String[])type.getColumnNames().clone();
			for(int i=0; i<columnNames.length; i++) {
				columnNames[i] = baseColumnName + "_" + columnNames[i];
			}
		}
		
		public String[] getColumnNames() {
			return columnNames;
		}
		
		public Class[] getJavaTypes() {
			return type.getJavaTypes();
		}
		
		public int[] getJDBCTypes() {
			return type.getJDBCTypes();
		}
		
		public String[] getSQLTypes() {
			return type.getSQLTypes();
		}
		
		public Object getColumnValue(int index, Object value) {
			return type.getColumnValue(index, value);
		}
		
		public Object setColumnValue(int index, Object value, Object columnValue) {
			return type.setColumnValue(index, value, columnValue);
		}
	}

	//
	//
	//  Uninteresting deligates to cmpField below.
	//
	//
	public String getFieldName() {
		return cmpField.getFieldName();
	}
	
	public Class getFieldType() {
		return cmpField.getFieldType();
	}

	public Object getPrimaryKeyValue(Object primaryKey) throws IllegalArgumentException {
		return cmpField.getPrimaryKeyValue(primaryKey);
	}

	public Object setPrimaryKeyValue(Object primaryKey, Object value) throws IllegalArgumentException {
		return cmpField.setPrimaryKeyValue(primaryKey, value);
	}

	public int setPrimaryKeyParameters(PreparedStatement ps, int parameterIndex, Object primaryKey) throws IllegalArgumentException {
		return cmpField.setPrimaryKeyParameters(ps, parameterIndex, primaryKey);
	}
	
	public int setArgumentParameters(PreparedStatement ps, int parameterIndex, Object arg) {
		return cmpField.setArgumentParameters(ps, parameterIndex, arg);
	}	

	public int loadPrimaryKeyResults(ResultSet rs, int parameterIndex, Object[] pkRef) throws IllegalArgumentException {
		return cmpField.loadPrimaryKeyResults(rs, parameterIndex, pkRef);
	}	
}
                                         