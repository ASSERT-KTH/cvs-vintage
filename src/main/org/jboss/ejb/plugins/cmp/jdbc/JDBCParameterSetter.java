/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.StringReader;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;

/**
 * Implementations of this interface are used to set java.sql.PreparedStatement parameters.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface JDBCParameterSetter
{
   /**
    * Sets a parameter of a specific JDBC type.
    * @param ps  the java.sql.PreparedStatement to set parameter on
    * @param index  the index of the parameter
    * @param jdbcType  the JDBC type of the parameter as defined by java.sql.Types
    * @param value  parameter value
    * @param log  the logger
    * @throws SQLException
    */
   void set(PreparedStatement ps, int index, int jdbcType, Object value, Logger log) throws SQLException;

   abstract class JDBCAbstractParameterSetter implements JDBCParameterSetter
   {
      public void set(PreparedStatement ps, int index, int jdbcType, Object value, Logger log)
         throws SQLException
      {
         if(log.isTraceEnabled())
         {
            log.trace("param: " +
               "i=" + index + ", " +
               "type=" + JDBCUtil.getJDBCTypeName(jdbcType) + ", " +
               "value=" + ((value == null) ? "NULL" : value));
         }

         if(value == null)
         {
            ps.setNull(index, jdbcType);
         }
         else
         {
            value = JDBCUtil.coerceToSQLType(jdbcType, value);
            setNotNull(ps, index, jdbcType, value, log);
         }
      }

      protected abstract void setNotNull(PreparedStatement ps, int index, int jdbcType, Object value, Logger log)
         throws SQLException;
   }

   /**
    * Types.CLOB, Types.LONGVARCHAR.
    */
   JDBCParameterSetter CLOB = new JDBCAbstractParameterSetter()
   {
      protected void setNotNull(PreparedStatement ps, int index, int jdbcType, Object value, Logger log)
         throws SQLException
      {
         String string = value.toString();
         ps.setCharacterStream(index, new StringReader(string), string.length());
      }
   };

   /**
    * Types.BINARY, Types.VARBINARY.
    */
   JDBCParameterSetter BINARY = new JDBCAbstractParameterSetter()
   {
      protected void setNotNull(PreparedStatement ps, int index, int jdbcType, Object value, Logger log)
         throws SQLException
      {
         byte[] bytes = JDBCUtil.convertObjectToByteArray(value);
         ps.setBytes(index, bytes);
      }
   };

   /**
    * Types.BLOB, Types.LONGVARBINARY.
    */
   JDBCParameterSetter BLOB = new JDBCAbstractParameterSetter()
   {
      protected void setNotNull(PreparedStatement ps, int index, int jdbcType, Object value, Logger log)
         throws SQLException
      {
         byte[] bytes = JDBCUtil.convertObjectToByteArray(value);
         ps.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
      }
   };

   /**
    * Types.DECIMAL, Types.NUMERIC
    */
   JDBCParameterSetter NUMERIC = new JDBCAbstractParameterSetter()
   {
      protected void setNotNull(PreparedStatement ps, int index, int jdbcType, Object value, Logger log)
         throws SQLException
      {
         if(value instanceof BigDecimal)
         {
            ps.setBigDecimal(index, (BigDecimal)value);
         }
         else
         {
            ps.setObject(index, value, jdbcType, 0);
         }
      }
   };

   /**
    * Types.JAVA_OBJECT, Types.OTHER, Types.STRUCT
    */
   JDBCParameterSetter OBJECT = new JDBCAbstractParameterSetter()
   {
      protected void setNotNull(PreparedStatement ps, int index, int jdbcType, Object value, Logger log)
         throws SQLException
      {
         ps.setObject(index, value, jdbcType);
      }
   };
}
