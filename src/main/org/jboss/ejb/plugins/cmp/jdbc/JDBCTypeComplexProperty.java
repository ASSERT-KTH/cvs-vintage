/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import javax.ejb.EJBException;
import java.lang.reflect.Method;

/**
 * JDBCTypeComplexProperty contins the mapping between a single Java Bean
 * (not an EJB) property and a column. This class has a flattened view of
 * the Java Bean property, which may be several properties deep in the 
 * base Java Bean. The details of how a property is mapped to a column 
 * can be found in JDBCTypeFactory.
 *
 * This class holds a description of the column and, knows how to extract
 * the column value from the Java Bean and how to set a column value info
 * the Java Bean.
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public class JDBCTypeComplexProperty {
	private String propertyName;
	private String columnName;	
	private Class javaType;	
	private int jdbcType;	
	private String sqlType;
	
	private Method[] getters;
	private Method[] setters;

	public String getPropertyName() {
		return propertyName;
	}
	
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public Class getJavaType() {
		return javaType;
	}
	
	public void setJavaType(Class javaType) {
		this.javaType = javaType;
	}
	
	public int getJDBCType() {
		return jdbcType;
	}
	
	public void setJDBCType(int jdbcType) {
		this.jdbcType = jdbcType;
	}
	
	public String getSQLType() {
		return sqlType;
	}
	
	public void setSQLType(String sqlType) {
		this.sqlType = sqlType;
	}
	
	public void setGetters(Method[] getters) {
		this.getters = getters;
	}
	
	public void setSetters(Method[] setters) {
		this.setters = setters;
	}
	
	public Object getColumnValue(Object value) throws Exception {
		Object[] noArgs = new Object[0];
		
		for(int i=0; i<getters.length; i++) {
			if(value == null) {
				return null;
			}
			value = getters[i].invoke(value, noArgs);
		}
		return value;
	}

	public Object setColumnValue(Object value, Object columnValue) throws Exception {
		// Used for invocation of get and set
		Object[] noArgs = new Object[0];
		Object[] singleArg = new Object[1];
		
		// save the first value to return
		Object returnValue = value;

		// get the second to last object in the chain
		for(int i=0; i<getters.length-1; i++) {
			// get the next object in chain
			//Logger.debug("Calling " + getters[i].getName() + " on " + value.getClass().getName());
			Object next = getters[i].invoke(value, noArgs);
			
			// the next object is null creat it
			if(next == null) {
				// new type based on getter
				next = getters[i].getReturnType().newInstance();
				
				// and set it into the current value
				singleArg[0] = next;
				
				//Logger.debug("Calling " + setters[i].getName() + " on " + value.getClass().getName());
				setters[i].invoke(value, singleArg);
			}
			
			// update value to the next in chain
			value = next;
		}
		
		// value is now the object on which we need to set the column value
		singleArg[0] = columnValue;
		//Logger.debug("Calling " + setters[setters.length-1].getName() + " on " + value.getClass().getName());
		setters[setters.length-1].invoke(value, singleArg);
		
		// return the first object in call chain
		return returnValue;
	}
}
