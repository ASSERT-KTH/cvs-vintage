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
 * mapping of java classes to multiple columns.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:loubyansky@ua.fm">Alex Loubyansky</a>
 * @version $Revision: 1.9 $
 */
public interface JDBCType
{
   String[] getColumnNames();

   Class[] getJavaTypes();

   int[] getJDBCTypes();

   String[] getSQLTypes();

   boolean[] getNotNull();

   boolean[] getAutoIncrement();

   JDBCResultSetReader[] getResultSetReaders();

   JDBCParameterSetter[] getParameterSetter();

   Object getColumnValue(int index, Object value);

   Object setColumnValue(int index, Object value, Object columnValue);
}
