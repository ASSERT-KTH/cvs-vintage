/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;

/**
 * JDBCValuePropertyMetaData contains information about a single dependent
 * value object property.
 *     
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCValuePropertyMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	
	private String propertyName;
	private Class propertyType;
	private String columnName;
	private String sqlType;
	// default value used is intended to cause an exception if used
	private int jdbcType = Integer.MIN_VALUE;
	private Method getter;
	private Method setter;
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
   
	// Public --------------------------------------------------------
	public JDBCValuePropertyMetaData(Element propertyElement, JDBCValueClassMetaData valueClass) throws DeploymentException {
		Class classType = valueClass.getJavaType();
		
		propertyName = getElementContent(getUniqueChild(propertyElement, "property-name"));

		columnName = getElementContent(getOptionalChild(propertyElement, "column-name"));
		if(columnName == null) {
			columnName = propertyName;
		}

		// resolve getter
		try {
			getter = classType.getMethod(toGetterName(propertyName), new Class[0]);
		} catch(Exception e) {
			throw new DeploymentException("Unable to find getter for property " +
					propertyName + " on dependent value class " + classType.getName());
		}

		// get property type from getter return type
		propertyType = getter.getReturnType();
		
		// resolve setter
		try {
			setter = classType.getMethod(
					toSetterName(propertyName), 
					new Class[] { propertyType }  );
		} catch(Exception e) {
			throw new DeploymentException("Unable to find getter for property " +
					propertyName + " on dependent value class " + classType.getName());
		}

		// jdbc type - optional
		String jdbcString = getElementContent(getOptionalChild(propertyElement, "jdbc-type"));
		if(jdbcString != null) {
			jdbcType = JDBCMappingMetaData.getJdbcTypeFromName(jdbcString); 
			
			// sql type - required if jdbc-type specified
			sqlType = getElementContent(getUniqueChild(propertyElement, "sql-type"));
		}
	}
	
	public String getPropertyName() {
		return propertyName;
	}

	public Class getPropertyType() {
		return propertyType;
	}

	public String getColumnName() {
		return columnName;
	}
	
	public int getJDBCType() {
		return jdbcType;
	}

	public String getSqlType() {
		return sqlType;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}
	
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------
	protected String toGetterName(String propertyName) {
		return "get" + upCaseFirstCharacter(propertyName); 
	}
	
	protected String toSetterName(String propertyName) {
		return "set" + upCaseFirstCharacter(propertyName); 
	}
	
	protected String upCaseFirstCharacter(String propertyName) {
		return Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
	}
	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------
}
