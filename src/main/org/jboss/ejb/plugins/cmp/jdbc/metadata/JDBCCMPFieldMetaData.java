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
import java.util.Iterator;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;

/**
 *	This class holds all the information jbosscmp-jdbc needs to know about a CMP field
 * It loads its data from standardjbosscmp-jdbc.xml and jbosscmp-jdbc.xml
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:vincent.harcq@hubmethods.com">Vincent Harcq</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCCMPFieldMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	
	protected Class entityClass;
	
	// name of the field
   protected String fieldName;

	// the actual Field in the bean implementation
	private Class fieldType;
	
	// is this field read only
	private boolean readOnly;
	
	// how long is read valid
	private int readTimeOut = -1;

	// the jdbc type (see java.sql.Types), used in PreparedStatement.setParameter
	// default value used is intended to cause an exception if used
	private int jdbcType = Integer.MIN_VALUE;

	// the sql type, used for table creation.
	private String sqlType;

	// the column name in the table
	private String columnName;

   // is this field a memeber of the primary key class
	// or the prim-key-field
	protected boolean primaryKeyMember;
	
	// the field in the primary key for this cmp field
	// or null if this field is the prim-key-field
	protected Field primaryKeyField;
	
	// the entity this field belongs to
	private JDBCEntityMetaData jdbcEntity;
	
	// property overrides
	private ArrayList propertyOverrides = new ArrayList();


	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
	public JDBCCMPFieldMetaData(String fieldName, JDBCEntityMetaData jdbcEntity) throws DeploymentException {
		this.fieldName = fieldName;
		this.jdbcEntity = jdbcEntity;

		entityClass = jdbcEntity.getEntityClass();
      fieldType = loadFieldType();

		// default, may be overridden by importXml
		columnName = fieldName;

		initializePrimaryKeyMetaData();
	}


	// Public --------------------------------------------------------
	public String getFieldName() {
		return fieldName;
	}

	public Class getFieldType() {
		return fieldType;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
	
	public int getReadTimeOut() {
		return readTimeOut;
	}
	
	public int getJDBCType() {
		return jdbcType;
	}

	public String getSQLType() {
		return sqlType;
	}

	public String getColumnName() {
		return columnName;
	}

	public boolean isPrimaryKeyMember() {
		return primaryKeyMember;
	}

	public Field getPrimaryKeyField() {
		return primaryKeyField;
	}
	
	public JDBCEntityMetaData getJDBCEntity() {
		return jdbcEntity;
	}
	
	public Iterator getPropertyOverrides() {
		return propertyOverrides.iterator();
	}

	// XmlLoadable implementation ------------------------------------
	public void importXml(Element element) throws DeploymentException {
		// read-only
		String readOnlyStr = getElementContent(getOptionalChild(element, "read-only"));
		if(readOnlyStr != null) {
			readOnly = Boolean.valueOf(readOnlyStr).booleanValue();
		} else {
			// use the entity default value
			readOnly = getJDBCEntity().isReadOnly();
		}

		// read-time-out
		if(isReadOnly()) {
			// read-time-out
			String readTimeOutStr = getElementContent(getOptionalChild(element, "read-time-out"));
		   if(readTimeOutStr != null) {
				readTimeOut = Integer.parseInt(readTimeOutStr);
			} else {
				// use the entity default value
				readTimeOut = getJDBCEntity().getReadTimeOut();
			}
		}		

		// column name
		String columnStr = getElementContent(getOptionalChild(element, "column-name"));
		if(columnStr != null) {
			columnName = columnStr;
		}

		// jdbc type
		String jdbcStr = getElementContent(getOptionalChild(element, "jdbc-type"));
		if(jdbcStr != null) {
			jdbcType =  JDBCMappingMetaData.getJdbcTypeFromName(jdbcStr);
			sqlType = getElementContent(getUniqueChild(element, "sql-type"));
		}

		// property overrides
		Iterator iterator = getChildrenByTagName(element, "property");
      while(iterator.hasNext()) {
			propertyOverrides.add(new JDBCCMPFieldPropertyMetaData((Element)iterator.next()));
		}
	}

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	protected Class loadFieldType() throws DeploymentException {
		if(jdbcEntity.isCMP1x()) {
			
			// CMP 1.x field Style
			try {
				return jdbcEntity.getEntityClass().getField(fieldName).getType();
			} catch(NoSuchFieldException e) {
				throw new DeploymentException("No field named '" + fieldName + "' found in entity class.");
			}
		} else {
			
			// CMP 2.x abstract accessor style
			String baseName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			String getName = "get" + baseName;
			String setName = "set" + baseName;
			
			Method[] methods = jdbcEntity.getEntityClass().getMethods();
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

	protected void initializePrimaryKeyMetaData() throws DeploymentException {
		String pkFieldName = jdbcEntity.getEntity().getPrimKeyField();
		if(pkFieldName != null) {
			if(pkFieldName.equals(fieldName)) {
				
				// verify field type
				if(!jdbcEntity.getPrimaryKeyClass().equals(fieldType)) {
					throw new DeploymentException("primkey-field must be the same type as prim-key-class"); 
				}
				// we are the pk
				primaryKeyMember = true;
			} else {
				primaryKeyMember = false;
			}
		} else {
			// this is a multi-valued key
			Field[] fields = jdbcEntity.getPrimaryKeyClass().getFields();
			for(int i=0; i<fields.length; i++) {
				if(fields[i].getName().equals(fieldName)) {
					
					// verify field type
					if(!fields[i].getType().equals(fieldType)) {
						throw new DeploymentException("Field " + fieldName + " in prim-key-class must be the same type");
					}
						
					// we are a pk member
					primaryKeyMember = true;
					primaryKeyField = fields[i];
				}
			}
		}
	}

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
