/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

/**
 * This interface represents a mapping between a Java type and JDBC type.
 * The properties all return arrays, because this type system supports the
 * mapping of java classes to multipul columns.
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public interface JDBCType {
   public String[] getColumnNames();   
   public Class[] getJavaTypes();   
   public int[] getJDBCTypes();   
   public String[] getSQLTypes();
   public boolean[] getNotNull();
   
   public Object getColumnValue(int index, Object value);
   public Object setColumnValue(int index, Object value, Object columnValue);
}
