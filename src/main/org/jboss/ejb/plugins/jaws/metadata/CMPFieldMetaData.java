/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.metadata;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.EjbRefMetaData;



/**
 *	This class holds all the information jaws needs to know about a CMP field
 *  It loads its data from standardjaws.xml and jaws.xml
 *      
 *	@see <related>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.1 $
 */
public class CMPFieldMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
    
	// Attributes ----------------------------------------------------
	
	// the entity this field belongs to
	private JawsEntityMetaData jawsEntity;
	
	// name of the field
    private String name;
	
	// the actual Field in the bean implementation
	private Field field;
	
	// the jdbc type (see java.sql.Types), used in PreparedStatement.setParameter
	private int jdbcType;
	// true if jdbcType has been initialized
	private boolean validJdbcType;
	
	// the sql type, used for table creation.	
	private String sqlType;
	
	// the column name in the table
	private String columnName;
	
	private boolean isAPrimaryKeyField;
	
	
	// Static --------------------------------------------------------
   
	// Constructors --------------------------------------------------
	public CMPFieldMetaData(String name, JawsEntityMetaData jawsEntity) throws DeploymentException {
		this.name = name;
		this.jawsEntity = jawsEntity;
		
		String ejbClassName = jawsEntity.getEntity().getEjbClass();
		try {
			Class ejbClass = jawsEntity.getJawsApplication().getClassLoader().loadClass(ejbClassName);
		    field = ejbClass.getField(name);
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("ejb class not found: " + ejbClassName);
		} catch (NoSuchFieldException e) {
			throw new DeploymentException("cmp-field " + name + " is not a field in ejb class " + ejbClassName);
		}
		
		// default, may be overridden by importXml
		columnName = name;
		
		// cannot set defaults for jdbctype/sqltype, type mappings are not loaded yet.
	}
   
   
	// Public --------------------------------------------------------
	public String getName() { return name; }
	
	public Field getField() { return field; }

	public int getJDBCType() { 
		if (! validJdbcType) {
			// set the default
			jdbcType = jawsEntity.getJawsApplication().getTypeMapping().getJdbcTypeForJavaType(field.getType());
			validJdbcType = true;
		}
		return jdbcType;
	}
	
	public String getSQLType() { 
		if (sqlType == null) {
			// set the default
			sqlType = jawsEntity.getJawsApplication().getTypeMapping().getSqlTypeForJavaType(field.getType());
		}
		return sqlType;
	}

	public String getColumnName() { return columnName; }
	
	public boolean isEJBReference() { return jdbcType == Types.REF; }
	
	public boolean isAPrimaryKeyField() { return isAPrimaryKeyField; }
	
	public JawsEntityMetaData getJawsEntity() { return jawsEntity; }
		
	
	// XmlLoadable implementation ------------------------------------
	public void importXml(Element element) throws DeploymentException {
		
		// column name
		String columnStr = getElementContent(getOptionalChild(element, "column-name"));
		if (columnStr != null) columnName = columnStr;


		// jdbc type
		String jdbcStr = getElementContent(getOptionalChild(element, "jdbc-type"));

		if (jdbcStr != null) {
			jdbcType = MappingMetaData.getJdbcTypeFromName(jdbcStr);
			validJdbcType = true;

			sqlType = getElementContent(getUniqueChild(element, "sql-type"));
		}
		
	}
    	
		
	// Package protected ---------------------------------------------
	void setPrimary() {
		isAPrimaryKeyField = true;
	}
    
	// Protected -----------------------------------------------------
    
	// Private -------------------------------------------------------
		
	// Inner classes -------------------------------------------------
}
