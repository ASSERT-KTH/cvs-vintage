/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
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

import java.util.*;

import org.jboss.logging.Log;
import org.jboss.logging.Logger;

/**
 *	This class holds all the information jaws needs to know about a CMP field
 *  It loads its data from standardjaws.xml and jaws.xml
 *      
 *	@see <related>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *  @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 *	@version $Revision: 1.4 $
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
	
	
	/**
	 * We need this for nested field retrieval.
	 */
	private String ejbClassName;

	/**
	 * We need this for nested fields. We could compute it from ejbClassName on the fly,
	 * but it's faster to set it once and cache it.
	 */
	private Class ejbClass;

	/**
	 * Is true for fields like "data.categoryPK".
	 */
	private boolean isNested;

	// Static --------------------------------------------------------
   
	// Constructors --------------------------------------------------
	public CMPFieldMetaData(String name, JawsEntityMetaData jawsEntity) throws DeploymentException {
		this.name = name;
		this.jawsEntity = jawsEntity;
		
		// save the class name for nested fields
		ejbClassName = jawsEntity.getEntity().getEjbClass();
		ejbClassName = jawsEntity.getEntity().getEjbClass();

		try {
			// save the class for nested fields
			ejbClass = jawsEntity.getJawsApplication().getClassLoader().loadClass(ejbClassName);
		    field = ejbClass.getField(name);
		} catch (ClassNotFoundException e) {
			throw new DeploymentException("ejb class not found: " + ejbClassName);
		} catch (NoSuchFieldException e) {
			// we can't throw an Exception here, because we could have a nested field
			checkField();
		}
		
		// default, may be overridden by importXml
		columnName = getLastComponent(name);
		
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
		
	/**
	 * Returns the last component of a composite fieldName. E.g., for "data.categoryPK" it
	 * will return "categoryPK".
	 */
	public static String getLastComponent(String name) {
		String fieldName = name;
		StringTokenizer st = new StringTokenizer(name, ".");
		while(st.hasMoreTokens()) {
			fieldName = st.nextToken();
		}
		return fieldName;
	}

	/**
	 * Returns the first component of a composite fieldName. E.g., for "data.categoryPK" it
	 * will return "data".
	 */
	public static String getFirstComponent(String name) {
		String fieldName;
		StringTokenizer st = new StringTokenizer(name, ".");
		if (st.hasMoreTokens()) {
			fieldName = st.nextToken();
		}
		else {
			fieldName = null;
		}
		return fieldName;
	}

	/**
	 * Detects the actual field of a nested field and sets field accordingly.
	 * If field doesn't exist, throws a DeploymentException.
	 */
	private void checkField()	throws DeploymentException {
		try {
			field = verifyNestedField();
		}
		catch(DeploymentException e) {
			// try it again, but debug Class before :))
			debugClass(ejbClass);
			field = verifyNestedField();
			Log.getLog().warning("!!! using buggy hotspot, try to upgrade ... !!!");
		}
	}

	/**
	 * Traverses and verifies a nested field, so that every field given in jaws.xml
	 * exists in the Bean.
	 */
	private Field verifyNestedField() throws DeploymentException {
		String fieldName = null;
		Field tmpField = null;
		Class tmpClass = ejbClass;
		StringTokenizer st = new StringTokenizer(name, ".");

		if (st.countTokens() > 1) {
			isNested = true;
		}

		while(st.hasMoreTokens()) {
			fieldName = st.nextToken();
			try {
				//debugClass(tmpClass);
				tmpField = tmpClass.getField(fieldName);
				tmpClass = tmpField.getType();
			}
			catch (NoSuchFieldException e) {
				throw new DeploymentException("cmp-field " + name + " is not a field in ejb class " + ejbClassName);
			}
		}
		return tmpField;
	}

	/**
	 * We don't rely on the field alone for getting the type since we support nested field
	 * like 'data.categoryPK'.
	 */
	public Class getFieldType()	{
		if (field != null) {
			// The default case as it always was :)
			return field.getType();
		}
				
		// We obviously have a nested field (or an erroneous one)
		Field tmpField = null;
		Class tmpClass = ejbClass;
		String fieldName = null;
		StringTokenizer st = new StringTokenizer(name, ".");
		while(st.hasMoreTokens()) {
			fieldName = st.nextToken();
			try {
				tmpField = tmpClass.getField(fieldName);
				tmpClass = tmpField.getType();
			}
			catch (NoSuchFieldException e) {
				Log.getLog().warning("!!! Deployment Failure !!!");
			}
		}
		return tmpField.getType();
	}

	/**
	 * Is used mainly for nested fields. Sets the value of a nested field.
	 */
	public void set(Object instance, Object value) {
		Field tmpField = null;
		String fieldName = null;
		Object currentObject = instance;
		Object oldObject;
		StringTokenizer st = new StringTokenizer(name, ".");

		try {
			for (int i = 0; i < st.countTokens() - 1; ++i) {
				st.hasMoreTokens();
				fieldName = st.nextToken();
				tmpField = currentObject.getClass().getField(fieldName);
				oldObject = currentObject;
				currentObject = tmpField.get(currentObject);
				// On our path, we have to instantiate every intermediate object
				if (currentObject == null) {
					currentObject = tmpField.getType().newInstance();
					tmpField.set(oldObject, currentObject);
				}
			}
			Field dataField = currentObject.getClass().getField(getLastComponent(name));
			dataField.set(currentObject, value);
		}
		catch (NoSuchFieldException e) {
			Log.getLog().warning("!!! Deployment Failure !!!");
		}
		catch (IllegalAccessException e) {
			Log.getLog().warning("!!! Deployment Failure !!!");
		}
		catch (InstantiationException e) {
			Log.getLog().warning("could not instantiate " + tmpField);
		}
	}

	/**
	 * Returns the value of this field.
	 */
	public Object getValue(Object instance) {
		String fieldName;
		Object currentObject = instance;
		Field currentField;
		//Object currentValue = null;

		try {
			if (!isNested()) {
				return getField().get(instance);
			}
			else {
				StringTokenizer st = new StringTokenizer(name, ".");
				while(st.hasMoreTokens()) {
					fieldName = st.nextToken();
					currentField = currentObject.getClass().getField(fieldName);
					currentObject = currentField.get(currentObject);
				}
				return currentObject;
			}
		}
		catch (IllegalAccessException e) {
			// We have already checked the presence of this field in the constructor,
			// so there is no need to throw an exception here.
			Log.getLog().warning("!!! CMPFieldMetaData.getValue() ERROR !!! " + e);
		}
		catch (NoSuchFieldException e) {
			// We have already checked the presence of this field in the constructor,
			// so there is no need to throw an exception here.
			Log.getLog().warning("!!! CMPFieldMetaData.getValue() ERROR !!! " + e);
		}
		return null;
	}

	public boolean isNested() { 
		return isNested; 
	}

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

	/**
	 * Workaround for certain Hotspot problems. Just traverse all the fields
	 * in the Class, so Hotspot won't optimize to bad ...
	 */
	private void debugClass(Class debugClass) {
		Field[] fields = debugClass.getFields();
		for (int i = 0; i < fields.length; ++i) {
		}
	}
		
	// Inner classes -------------------------------------------------
}
