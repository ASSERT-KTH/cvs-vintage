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
 * JDBCCMPFieldBridge represents one CMP field. This class handles setting 
 * statement parameters and loading results for instance values and primary
 * keys. Most of the heavy lifting of this command is handled by JDBCUtil.
 * It is left to subclasses to implement the logic for getting and setting 
 * instance values and dirty checking, as this is dependent on the CMP 
 * version used.
 *
 * Life-cycle:
 *		Tied to the EntityBridge.
 *
 * Multiplicity:	
 *		One for each entity bean cmp field. 		
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */                            
public abstract class JDBCCMPFieldBridge implements CMPFieldBridge {
	protected JDBCStoreManager manager;
   protected JDBCCMPFieldMetaData metadata;
	protected JDBCType jdbcType;
	protected Log log;
	
	public JDBCCMPFieldBridge(JDBCStoreManager manager, JDBCCMPFieldMetaData metadata, Log log) throws DeploymentException {
		this.manager = manager;
		this.metadata = metadata;
		this.log = log;
		
		jdbcType =  manager.getJDBCTypeFactory().getFieldJDBCType(metadata);
	}

   public JDBCCMPFieldMetaData getMetaData() {
		return metadata;
	}

	public String getFieldName() {
		return metadata.getFieldName();
	}
	
	public Class getFieldType() {
		return metadata.getFieldType();
	}

	public boolean isReadOnly() {
		return metadata.isReadOnly();
	}

	public abstract boolean isReadTimedOut(EntityEnterpriseContext ctx);
	
	public boolean isPrimaryKeyMember() {
		return metadata.isPrimaryKeyMember();
	}

	public Object getPrimaryKeyValue(Object primaryKey) throws IllegalArgumentException {
		if(!isPrimaryKeyMember()) {
			throw new IllegalArgumentException(getFieldName() + " is not a member of the primary key.");
		}
		
		try {
			if(metadata.getPrimaryKeyField() != null) {
				// Extract this field's value from the primary key.
				return metadata.getPrimaryKeyField().get(primaryKey);
			} else {
				// This field is the primary key, so no extraction is necessary.
				return primaryKey;
			}
		} catch(Exception e) {
			// Non recoverable internal exception
			throw new EJBException("Internal error getting primary key field member " + getFieldName() + ": " + e);
		}
	}

	public Object setPrimaryKeyValue(Object primaryKey, Object value) throws IllegalArgumentException {
		if(!isPrimaryKeyMember()) {
			throw new IllegalArgumentException(getFieldName() + " is not a member of the primary key.");
		}
		
		try {
			if(metadata.getPrimaryKeyField() != null) {
				// Extract this field's value from the primary key.
				metadata.getPrimaryKeyField().set(primaryKey, value);
				return primaryKey;
			} else {
				// This field is the primary key, so no extraction is necessary.
				return value;
			}
		} catch(Exception e) {
			// Non recoverable internal exception
			throw new EJBException("Internal error setting instance field " + getFieldName() + ": " + e);
		}
	}

	public abstract void resetPersistenceContext(EntityEnterpriseContext ctx);
	
	/**
	* Set CMPFieldValue to Java default value (i.e., 0 or null).
	*/
	public void initInstance(EntityEnterpriseContext ctx) {
		if(!isReadOnly()) {
			Object value;
			if(getFieldType().equals(boolean.class))  {
				value = Boolean.FALSE;
			} else if(getFieldType().equals(byte.class))  {
				value = new Byte((byte)0);
			} else if (getFieldType().equals(int.class))  {
				value = new Integer(0);
			} else if (getFieldType().equals(long.class))  {
				value = new Long(0L);
			} else if (getFieldType().equals(short.class))  {
				value = new Short((short)0);
			} else if (getFieldType().equals(char.class))  {
				value = new Character('\u0000');
			} else if (getFieldType().equals(double.class))  {
				value = new Double(0d); 
			} else if (getFieldType().equals(float.class))  {
				value = new Float(0f);
			} else  {
				value = null;
			}
	
			setInstanceValue(ctx, value);
		}
	}		

	public JDBCType getJDBCType() {
		return jdbcType;
	}

	public int setInstanceParameters(PreparedStatement ps, int parameterIndex, EntityEnterpriseContext ctx) {
		Object instanceValue = getInstanceValue(ctx);
		return setArgumentParameters(ps, parameterIndex, instanceValue);
	}	

	public int setPrimaryKeyParameters(PreparedStatement ps, int parameterIndex, Object primaryKey) throws IllegalArgumentException {
		if(!isPrimaryKeyMember()) {
			throw new IllegalArgumentException(getFieldName() + " is not a member of the primary key.");
		}
		Object primaryKeyValue = getPrimaryKeyValue(primaryKey);
		return setArgumentParameters(ps, parameterIndex, primaryKeyValue);
	}
	
	public int setArgumentParameters(PreparedStatement ps, int parameterIndex, Object arg) {
		try {
			int[] jdbcTypes = getJDBCType().getJDBCTypes();
			for(int i=0; i<jdbcTypes.length; i++) {
				Object columnValue = getJDBCType().getColumnValue(i, arg);
				JDBCUtil.setParameter(log, ps, parameterIndex++, jdbcTypes[i], columnValue);
			}
			return parameterIndex;
		} catch(SQLException e) {
			// Non recoverable internal exception
			throw new EJBException("Internal error setting parameters for field " + getFieldName() + ": " + e);
		}
	}	

	public int loadInstanceResults(ResultSet rs, int parameterIndex, EntityEnterpriseContext ctx) {
		try {
			Object value = getFieldType().newInstance();
			Class[] javaTypes = getJDBCType().getJavaTypes();
			for(int i=0; i<javaTypes.length; i++) {
				Object columnValue = JDBCUtil.getResult(log, rs, parameterIndex++, javaTypes[i]);
				value = getJDBCType().setColumnValue(i, value, columnValue);
			}
			setInstanceValue(ctx, value);
			return parameterIndex;
		} catch(EJBException e) {
			// to avoid double wrap of EJBExceptions
			throw e;
		} catch(Exception e) {
			// Non recoverable internal exception
			throw new EJBException("Internal error getting results for field " + getFieldName() + ": " + e);
		}
	}		
	
	public int loadPrimaryKeyResults(ResultSet rs, int parameterIndex, Object[] pkRef) throws IllegalArgumentException {
		if(!isPrimaryKeyMember()) {
			throw new IllegalArgumentException(getFieldName() + " is not a member of the primary key.");
		}

		try {
			// get this field's value object from the pk
			Object value = this.getPrimaryKeyValue(pkRef[0]);
			
			// update the value from the result set
			Class[] javaTypes = getJDBCType().getJavaTypes();
			for(int i=0; i<javaTypes.length; i++) {
				Object columnValue = JDBCUtil.getResult(log, rs, parameterIndex++, javaTypes[i]);
				value = getJDBCType().setColumnValue(i, value, columnValue);
			}
			
			// set the value back into the pk
			pkRef[0] = setPrimaryKeyValue(pkRef[0], value); 
			return parameterIndex;
		} catch(SQLException e) {
			// Non recoverable internal exception
			throw new EJBException("Internal error getting results for primary key field member " + getFieldName() + ": " + e);
		}
	}		

	protected final boolean changed(Object current, Object old) {
		return	
			(current == null && old != null) ||   // TRUE if I am null and I wasn't before   
	   	(current != null &&                   // TRUE if I was null and now I'm not
				( old == null || (!current.equals(old)) ) // TRUE if i'm not equal to my oldstate 
			);                                           //   tied to last check to assure that current is not null
				
	}
}
                                         