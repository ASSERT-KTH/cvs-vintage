/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

/**
 * JDBCTypeComplex provides the mapping between a Java Bean (not an EJB)
 * and a set of columns. This class has a flattened view of the Java Bean,
 * which may contain other Java Beans.  This class simply treats the bean
 * as a set of properties, which may be in the a.b.c style. The details
 * of how this mapping is performed can be found in JDBCTypeFactory.
 *
 * This class holds a description of the columns 
 * and the properties that map to the columns. Additionally, this class
 * knows how to extract a column value from the Java Bean and how to set
 * a column value info the Java Bean. See JDBCTypeComplexProperty for 
 * details on how this is done.
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public class JDBCTypeComplex implements JDBCType {
	private JDBCTypeComplexProperty[] properties;
	private String[] columnNames;	
	private Class[] javaTypes;	
	private int[] jdbcTypes;	
	private String[] sqlTypes;

   public JDBCTypeComplex(JDBCTypeComplexProperty[] properties) {
		this.properties = properties;
		
		columnNames = new String[properties.length];
		for(int i=0; i<columnNames.length; i++) {
			columnNames[i] = properties[i].getColumnName();
		}
		
		javaTypes = new Class[properties.length];
		for(int i=0; i<javaTypes.length; i++) {
			javaTypes[i] = properties[i].getJavaType();
		}
		
		jdbcTypes = new int[properties.length];
		for(int i=0; i<jdbcTypes.length; i++) {
			jdbcTypes[i] = properties[i].getJDBCType();
		}
		
		sqlTypes = new String[properties.length];
		for(int i=0; i<sqlTypes.length; i++) {
			sqlTypes[i] = properties[i].getSQLType();
		}
		
	}

	public String[] getColumnNames() {
		return columnNames;
	}
	
	public Class[] getJavaTypes() {
		return javaTypes;
	}
	
	public int[] getJDBCTypes() {
		return jdbcTypes;
	}
	
	public String[] getSQLTypes() {
		return sqlTypes;
	}
	
	public Object getColumnValue(int index, Object value) {
		return properties[index].getColumnValue(value);
	}

	public Object setColumnValue(int index, Object value, Object columnValue) {
		return properties[index].setColumnValue(value, columnValue);
	}
}
