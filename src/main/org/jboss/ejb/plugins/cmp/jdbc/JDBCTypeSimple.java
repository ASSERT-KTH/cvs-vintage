/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;


/**
 * This class provides a simple mapping of a Java type type to a single column.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 1.6 $
 */
public class JDBCTypeSimple implements JDBCType {
   private String[] columnNames;
   private Class[] javaTypes;
   private int[] jdbcTypes;
   private String[] sqlTypes;
   private boolean[] notNull;
   private boolean[] autoIncrement;

   private final Mapper mapper;

   public JDBCTypeSimple(
      String columnName,
      Class javaType,
      int jdbcType,
      String sqlType,
      boolean notNull,
      boolean autoIncrement,
      Mapper mapper
   )
   {

      columnNames = new String[] { columnName };
      javaTypes = new Class[] { javaType };
      jdbcTypes = new int[] { jdbcType };
      sqlTypes = new String[] { sqlType };
      this.notNull = new boolean[] { notNull };
      this.autoIncrement = new boolean[] { autoIncrement };
      this.mapper = mapper;
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

   public boolean[] getNotNull() {
      return notNull;
   }

   public boolean[] getAutoIncrement() {
      return autoIncrement;
   }

   public Object getColumnValue(int index, Object value) {
      if(index != 0) {
         throw new IndexOutOfBoundsException("JDBCSimpleType does not " +
               "support an index>0.");
      }
      return mapper == null ? value : mapper.toColumnValue(value);
   }

   public Object setColumnValue(int index, Object value, Object columnValue) {
      if(index != 0) {
         throw new IndexOutOfBoundsException("JDBCSimpleType does not " +
               "support an index>0.");
      }
      return mapper == null ? columnValue : mapper.toFieldValue(columnValue);
   }
}
