/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 *	Imutable class which holds all the information jbosscmp-jdbc needs to know about a CMP field
 * It loads its data from standardjbosscmp-jdbc.xml and jbosscmp-jdbc.xml
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:vincent.harcq@hubmethods.com">Vincent Harcq</a>
 *	@version $Revision: 1.2 $
 */
public final class JDBCCMPFieldMetaData {
	/**
	 * The entity on which this field is defined.
	 */
	private final JDBCEntityMetaData entity;
	
	/**
	 * The name of this field.
	 */
	private final String fieldName;
	
	/**
    *	The java type of this field 
	 */
	private final Class fieldType;
	
	/**
    * The column name in the table 
	 */
	private final String columnName;

	/**
	 * The jdbc type (see java.sql.Types), used in PreparedStatement.setParameter
	 * default value used is intended to cause an exception if used
	 */
	private final int jdbcType;

	/** 
	 * The sql type, used for table creation. 
	 */
	private final String sqlType;

	/**
    * Is this field read only?
	 */
	private final boolean readOnly;
	
	/**
    * How long is read valid
	 */
	private final int readTimeOut;

   /**
    * Is this field a memeber of the primary keys
	 * or the sole prim-key-field.
	 */
	private final boolean primaryKeyMember;
	
	/**
    * The Field object in the primary key class for this
	 * cmp field, or null if this field is the prim-key-field.
	 */
	private final Field primaryKeyField;
	
	/** 
	 * property overrides 
	 */
	private final List propertyOverrides = new ArrayList();

	/**
	 * Constructs cmp field meta data for a field on the specified entity with the 
	 * specified fieldName.
	 *
	 * @param fieldName name of the field for which the meta data will be loaded
	 * @param entity entity on which this field is defined
	 * @throws DeploymentException if data in the entity is inconsistent with field type
	 */
	public JDBCCMPFieldMetaData(JDBCEntityMetaData entity, String fieldName) throws DeploymentException {
		this.entity = entity;
		this.fieldName = fieldName;

      fieldType = loadFieldType(entity, fieldName);
		columnName = fieldName;
		jdbcType = Integer.MIN_VALUE;
		sqlType = null;
		readOnly = entity.isReadOnly();
		readTimeOut = entity.getReadTimeOut();

		// initialize primary key info
		String pkFieldName = entity.getPrimaryKeyFieldName();
		if(pkFieldName != null) {
			// single-valued key so field is null
			primaryKeyField = null;
			
			// is this the pk field
			if(pkFieldName.equals(fieldName)) {
				
				// verify field type
				if(!entity.getPrimaryKeyClass().equals(fieldType)) {
					throw new DeploymentException("primkey-field must be the same type as prim-key-class"); 
				}
				// we are the pk
				primaryKeyMember = true;
			} else {
				primaryKeyMember = false;
			}
		} else {
			// this is a multi-valued key
			Field[] fields = entity.getPrimaryKeyClass().getFields();

			boolean pkMember = false;
			Field pkField = null;
			for(int i=0; i<fields.length; i++) {
				if(fields[i].getName().equals(fieldName)) {
					
					// verify field type
					if(!fields[i].getType().equals(fieldType)) {
						throw new DeploymentException("Field " + fieldName + " in prim-key-class must be the same type");
					}
						
					// we are a pk member
					pkMember = true;
					pkField = fields[i];
				}
			}
			primaryKeyMember = pkMember;
			primaryKeyField = pkField;
		}
	}

	/**
	 * Constructs cmp field meta data with the data contained in the cmp-field xml 
	 * element from a jbosscmp-jdbc xml file. Optional values of the xml element that
	 * are not present are instead loaded from the defalutValues parameter.
	 *
	 * @param element the xml Element which contains the metadata about this field
	 * @param defaultValues the JDBCCMPFieldMetaData which contains the values
	 * 		for optional elements of the element
	 * @throws DeploymentException if the xml element is not semantically correct
	 */
	public JDBCCMPFieldMetaData(JDBCEntityMetaData entity, Element element, JDBCCMPFieldMetaData defaultValues) throws DeploymentException {
		this(entity, element, defaultValues, defaultValues.isPrimaryKeyMember());
	}

	/**
	 * Constructs cmp field meta data with the data contained in the cmp-field xml 
	 * element from a jbosscmp-jdbc xml file. Optional values of the xml element that
	 * are not present are instead loaded from the defalutValues parameter.
	 *
	 * This constructor form is used to create cmp field meta data for use as foreign keys.
	 * The primaryKeyMember parameter is very important in this context because a foreign key
	 * is not a primary key member but used a pk member as the default value.  If we did not have
	 * the primary key member parameter this JDBCCMPFieldMetaData would get the value from the
	 * defaultValues and be declared a memeber.
	 *
	 * @param element the xml Element which contains the metadata about this field
	 * @param defaultValues the JDBCCMPFieldMetaData which contains the values
	 * 		for optional elements of the element
	 * @param priamryKeyMember override the value of primary key member in the defaultValues
	 * @throws DeploymentException if the xml element is not semantically correct
	 */
	public JDBCCMPFieldMetaData(JDBCEntityMetaData entity, Element element, JDBCCMPFieldMetaData defaultValues, boolean primaryKeyMember) throws DeploymentException {
		this.entity = entity;

		// Field name
		fieldName = defaultValues.getFieldName();

		// Field type
      fieldType = defaultValues.getFieldType();

		// Column name
		String columnStr = MetaData.getOptionalChildContent(element, "column-name");
		if(columnStr != null) {
			columnName = columnStr;
		} else {
			columnName = defaultValues.getColumnName();
		}

		// JDBC Type
		String jdbcStr = MetaData.getOptionalChildContent(element, "jdbc-type");
		if(jdbcStr != null) {
			jdbcType =  JDBCMappingMetaData.getJdbcTypeFromName(jdbcStr);
			
			// SQL Type
			sqlType = MetaData.getUniqueChildContent(element, "sql-type");
		} else {
			jdbcType = defaultValues.getJDBCType();
			sqlType = defaultValues.getSQLType();
		}
		
		// read-only
		String readOnlyStr = MetaData.getOptionalChildContent(element, "read-only");
		if(readOnlyStr != null) {
			readOnly = Boolean.valueOf(readOnlyStr).booleanValue();
		} else {
			readOnly = defaultValues.isReadOnly();
		}

		// read-time-out
		String readTimeOutStr = MetaData.getOptionalChildContent(element, "read-time-out");
		if(readTimeOutStr != null) {
			readTimeOut = Integer.parseInt(readTimeOutStr);
		} else {
			readTimeOut = defaultValues.getReadTimeOut();
		}

		// primary key member?
		this.primaryKeyMember = primaryKeyMember;
		
		// field object of the primary key
		primaryKeyField = defaultValues.getPrimaryKeyField();

		// property overrides
		Iterator iterator = MetaData.getChildrenByTagName(element, "property");
      while(iterator.hasNext()) {
			propertyOverrides.add(new JDBCCMPFieldPropertyMetaData(this, (Element)iterator.next()));
		}
	}

	/**
	 * Constructs cmp field meta data with the data from the defaultValues parameter, except
	 * the columnName and primaryKeyMember are set from the parameters.
	 *
	 * This constructor form is used to create cmp field meta data for use as foreign keys.
	 * The primaryKeyMember parameter is very important in this context because a foreign key
	 * is not a primary key member but used a pk member as the default value.  If we did not have
	 * the primary key member parameter this JDBCCMPFieldMetaData would get the value from the
	 * defaultValues and be declared a memeber. The columnName prameter is similarly important
	 *
	 * @param element the xml Element which contains the metadata about this field
	 * @param defaultValues the JDBCCMPFieldMetaData which contains the values
	 * 		for optional elements of the element
	 * @param columnName overrides the value of the column name in the defaultValues
	 * @param priamryKeyMember override the value of primary key member in the defaultValues
	 * @throws DeploymentException if data in the entity is inconsistent with field type
	 */
	public JDBCCMPFieldMetaData(JDBCEntityMetaData entity, JDBCCMPFieldMetaData defaultValues, String columnName, boolean primaryKeyMember) {
		this.entity = entity;

		// Field name
		fieldName = defaultValues.getFieldName();

		// Field type
      fieldType = defaultValues.getFieldType();

		// Column name
		this.columnName = columnName;

		// JDBC Type
		jdbcType = defaultValues.getJDBCType();
		sqlType = defaultValues.getSQLType();
		
		// read-only
		readOnly = defaultValues.isReadOnly();

		// read-time-out
		readTimeOut = defaultValues.getReadTimeOut();

		// primary key member?
		this.primaryKeyMember = primaryKeyMember;
		
		// field object of the primary key
		primaryKeyField = defaultValues.getPrimaryKeyField();

		// property overrides
      for(Iterator i=defaultValues.propertyOverrides.iterator(); i.hasNext(); ) {
			propertyOverrides.add(new JDBCCMPFieldPropertyMetaData(this, (JDBCCMPFieldPropertyMetaData)i.next()));
		}

	}

	/**
	 * Gets the entity on which this field is defined
	 * @return the entity on which this field is defined
	 */
	public JDBCEntityMetaData getEntity() {
		return entity;
	}
	
	/**
	 * Gets the name of the field.
	 * @return the name of this field
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Gets the java Class type of this field.
	 * @return the Class type of this field
	 */
	public Class getFieldType() {
		return fieldType;
	}
	
	/**
	 * Gets the column name the property should use or null if the
	 * column name is not overriden. 
	 * @return the name to which this field is persisted or null if the
	 * 	column name is not overriden
	 */
	public String getColumnName() {
		return columnName;
	}
	
	/**
	 * Gets the JDBC type the property should use or Integer.MIN_VALUE 
	 * if not overriden.
	 * @return the jdbc type of this field
	 */
	public int getJDBCType() {
		return jdbcType;
	}

	/**
	 * Gets the SQL type the property should use or null 
	 * if not overriden.
	 * @return the sql data type string used in create table statements
	 */
	public String getSQLType() {
		return sqlType;
	}

	/**
	 * Gets the property overrides.  Property overrides change the default 
	 * mapping of Dependent Value Object properties. If there are no property
	 * overrides this method returns an empty list.
	 * @return an unmodifiable list of the property overrides.
	 */
	public List getPropertyOverrides() {
		return Collections.unmodifiableList(propertyOverrides);
	}
	
	/**
	 * Is this field read only. A read only field will never be persisted
	 *
	 * @return true if this field is read only
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
		
	/**
    * Gets the length of time (ms) that a read valid or -1 if data must 
	 * 		always be reread from the database
	 * @return the length of time that data read database is valid, or -1 
	 * 		if data must always be reread from the database
	 */
	public int getReadTimeOut() {
		return readTimeOut;
	}

	/**
	 * Is this field one of the primary key fields?
	 * @return true if this field is one of the primary key fields
	 */
	public boolean isPrimaryKeyMember() {
		return primaryKeyMember;
	}

	/**
	 * Gets the Field of the primary key object which contains the value of 
	 * this field. Returns null, if this field is not a member of the primary key, or if 
	 * the primray key is single valued.
	 * @return the Field of the primary key which contains the 
	 * 		value of this field
	 */
	public Field getPrimaryKeyField() {
		return primaryKeyField;
	}

	/**
	 * Compares this JDBCCMPFieldMetaData against the specified object. Returns
	 * true if the objects are the same. Two JDBCCMPFieldMetaData are the same 
	 * if they both have the same name and are defined on the same entity.
	 * @param o the reference object with which to compare
	 * @return true if this object is the same as the object argument; false otherwise
	 */
	public boolean equals(Object o) {
		if(o instanceof JDBCCMPFieldMetaData) {
			JDBCCMPFieldMetaData cmpField = (JDBCCMPFieldMetaData)o;
			return fieldName.equals(cmpField.fieldName) && entity.equals(cmpField.entity);
		}
		return false;
	}
	
	/**
	 * Returns a hashcode for this JDBCCMPFieldMetaData. The hashcode is computed
	 * based on the hashCode of the declaring entity and the hashCode of the fieldName
	 * @return a hash code value for this object
	 */
	public int hashCode() {
		int result = 17;
		result = 37*result + entity.hashCode();
		result = 37*result + fieldName.hashCode();
		return result;
	}
	/**
	 * Returns a string describing this JDBCCMPFieldMetaData. The exact details
	 * of the representation are unspecified and subject to change, but the following
	 * may be regarded as typical:
	 * 
	 * "[JDBCCMPFieldMetaData: fieldName=name,  [JDBCEntityMetaData: entityName=UserEJB]]"
	 *
	 * @return a string representation of the object
	 */
	public String toString() {
		return "[JDBCCMPFieldMetaData : fieldName=" + fieldName + ", " + entity + "]";
	}
	
	/**
	 * Loads the java type of this field from the entity bean class. If this bean uses, cmp 1.x
	 * persistence, the field type is loaded from the field in the bean class with the same name
	 * as this field. If this bean uses, cmp 2.x persistence, the field type is loaded from the
	 * abstract getter or setter method for field in the bean class.
	 */
	private Class loadFieldType(JDBCEntityMetaData entity, String fieldName) throws DeploymentException {
		if(entity.isCMP1x()) {
			
			// CMP 1.x field Style
			try {
				return entity.getEntityClass().getField(fieldName).getType();
			} catch(NoSuchFieldException e) {
				throw new DeploymentException("No field named '" + fieldName + "' found in entity class.");
			}
		} else {
			
			// CMP 2.x abstract accessor style
			String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			String getName = "get" + baseName;
			String setName = "set" + baseName;
			
			Method[] methods = entity.getEntityClass().getMethods();
			for(int i=0; i<methods.length; i++) {
				// is this a public abstract method?
				if(Modifier.isPublic(methods[i].getModifiers()) &&
						Modifier.isAbstract(methods[i].getModifiers())) {
					
					// get accessor
					if(getName.equals(methods[i].getName()) &&
							methods[i].getParameterTypes().length == 0 &&
							!methods[i].getReturnType().equals(Void.TYPE)) {
						
						return methods[i].getReturnType();
               } 
					
					// set accessor
					if(setName.equals(methods[i].getName()) &&
							methods[i].getParameterTypes().length == 1 &&
							methods[i].getReturnType().equals(Void.TYPE)) {
								
						return methods[i].getParameterTypes()[0];
					}
				}
			}
			throw new DeploymentException("No abstract accessors for field named '" + fieldName + "' found in entity class.");
		}
	}
}
