/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.logging.Logger;

import javax.ejb.Handle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Clob;
import java.sql.Blob;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;

/**
 * Implementations of this interface are used to read java.sql.ResultSet.
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
public interface JDBCResultSetReader
{
   /**
    * Reads one column from the java.sql.ResultSet.
    * @param rs  the java.sql.ResultSet to read from
    * @param index  the index of the column
    * @param destination  the expected Java class of result
    * @param log  the logger
    * @return  column value
    * @throws SQLException
    */
   Object get(ResultSet rs, int index, Class destination, Logger log) throws SQLException;

   public static final JDBCResultSetReader CLOB_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         Clob clob = rs.getClob(index);

         String content;
         if(clob == null)
         {
            content = null;
         }
         else
         {
            final Reader reader = clob.getCharacterStream();
            if(reader != null)
            {
               int intLength = (int)clob.length();

               char[] chars;
               try
               {
                  if(intLength <= 8192)
                  {
                     chars = new char[intLength];
                     reader.read(chars);
                     content = String.valueOf(chars);
                  }
                  else
                  {
                     StringBuffer buf = new StringBuffer(intLength);
                     chars = new char[8192];
                     int i = reader.read(chars);
                     while(i > 0)
                     {
                        buf.append(chars, 0, i);
                        i = reader.read(chars);
                     }
                     content = buf.toString();
                  }
               }
               catch(IOException e)
               {
                  throw new SQLException("Failed to read CLOB character stream: " + e.getMessage());
               }
               finally
               {
                  JDBCUtil.safeClose(reader);
               }
            }
            else
            {
               content = null;
            }
         }

         return content;
      }
   };

   public static final JDBCResultSetReader LONGVARCHAR_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return JDBCUtil.getLongString(rs, index);
      }
   };

   public static final JDBCResultSetReader BINARY_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         Object value = null;
         byte[] bytes = rs.getBytes(index);
         if(!rs.wasNull())
         {
            if(destination == byte[].class)
               value = bytes;
            else
               value = JDBCUtil.convertToObject(bytes);
         }
         return value;
      }
   };

   public static final JDBCResultSetReader VARBINARY_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         Object value = null;
         byte[] bytes = rs.getBytes(index);
         if(!rs.wasNull())
         {
            if(destination == byte[].class)
               value = bytes;
            else
               value = JDBCUtil.convertToObject(bytes);
         }
         return value;
      }
   };

   public static final JDBCResultSetReader BLOB_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         Blob blob = rs.getBlob(index);

         Object value;
         if(blob == null)
         {
            value = null;
         }
         else
         {
            InputStream binaryData = blob.getBinaryStream();
            if(binaryData != null)
            {
               try
               {
                  if(destination == byte[].class)
                     value = JDBCUtil.getByteArray(binaryData);
                  else
                     value = JDBCUtil.convertToObject(binaryData);
               }
               finally
               {
                  JDBCUtil.safeClose(binaryData);
               }
            }
            else
            {
               value = null;
            }
         }

         return value;
      }
   };

   public static final JDBCResultSetReader LONGVARBINARY_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         Object value = null;
         InputStream binaryData = rs.getBinaryStream(index);
         if(binaryData != null && !rs.wasNull())
         {
            try
            {
               if(destination == byte[].class)
                  value = JDBCUtil.getByteArray(binaryData);
               else
                  value = JDBCUtil.convertToObject(binaryData);
            }
            finally
            {
               JDBCUtil.safeClose(binaryData);
            }
         }
         return value;
      }
   };

   public static final JDBCResultSetReader JAVA_OBJECT_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getObject(index);
      }
   };

   public static final JDBCResultSetReader STRUCT_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getObject(index);
      }
   };

   public static final JDBCResultSetReader ARRAY_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getObject(index);
      }
   };

   public static final JDBCResultSetReader OTHER_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getObject(index);
      }
   };

   public static final JDBCResultSetReader JAVA_UTIL_DATE_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getTimestamp(index);
      }

      protected Object coerceToJavaType(Object value, Class destination)
      {
         // make new copy as sub types have problems in comparions
         java.util.Date result;
         // handle timestamp special becauses it hoses the milisecond values
         if(value instanceof java.sql.Timestamp)
         {
            java.sql.Timestamp ts = (java.sql.Timestamp)value;
            // Timestamp returns whole seconds from getTime and partial
            // seconds are retrieved from getNanos()
            // Adrian Brock: Not in 1.4 it doesn't
            long temp = ts.getTime();
            if(temp % 1000 == 0)
               temp += ts.getNanos() / 1000000;
            result = new java.util.Date(temp);
         }
         else
         {
            result = new java.util.Date(((java.util.Date)value).getTime());
         }
         return result;
      }
   };

   public static final JDBCResultSetReader JAVA_SQL_DATE_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getDate(index);
      }

      protected Object coerceToJavaType(Object value, Class destination)
      {
         // make a new copy object; you never know what a driver will return
         return new java.sql.Date(((java.sql.Date)value).getTime());
      }
   };

   public static final JDBCResultSetReader JAVA_SQL_TIME_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getTime(index);
      }

      protected Object coerceToJavaType(Object value, Class destination)
      {
         // make a new copy object; you never know what a driver will return
         return new java.sql.Time(((java.sql.Time)value).getTime());
      }
   };

   public static final JDBCResultSetReader JAVA_SQL_TIMESTAMP_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getTimestamp(index);
      }

      protected Object coerceToJavaType(Object value, Class destination)
      {
         // make a new copy object; you never know what a driver will return
         java.sql.Timestamp orignal = (java.sql.Timestamp)value;
         java.sql.Timestamp copy = new java.sql.Timestamp(orignal.getTime());
         copy.setNanos(orignal.getNanos());
         return copy;
      }
   };

   public static final JDBCResultSetReader BIGDECIMAL_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return rs.getBigDecimal(index);
      }
   };

   public static final JDBCResultSetReader REF_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return rs.getRef(index);
      }
   };

   public static final JDBCResultSetReader BYTE_ARRAY_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return rs.getBytes(index);
      }
   };

   public static final JDBCResultSetReader OBJECT_READER = new AbstractResultSetReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination) throws SQLException
      {
         return rs.getObject(index);
      }
   };

   public static final JDBCResultSetReader STRING_READER = new JDBCResultSetReader()
   {
      public Object get(ResultSet rs, int index, Class destination, Logger log) throws SQLException
      {
         final String result = rs.getString(index);

         if(log.isTraceEnabled())
         {
            log.trace("result: i=" + index + ", type=" + destination.getName() + ", value=" + result);
         }

         return result;
      }
   };

   abstract class AbstractPrimitiveReader
      extends AbstractResultSetReader
   {
      // ResultSetReader implementation

      public Object get(ResultSet rs, int index, Class destination, Logger log)
         throws SQLException
      {
         Object result = readResult(rs, index, destination);
         if(rs.wasNull())
            result = null;
         else
            result = coerceToJavaType(result, destination);

         if(log.isTraceEnabled())
         {
            log.trace("result: i=" + index + ", type=" + destination.getName() + ", value=" + result);
         }

         return result;
      }

      // Protected

      protected Object coerceToJavaType(Object value, Class destination)
         throws SQLException
      {
         return value;
      }
   }

   public static final JDBCResultSetReader BOOLEAN_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return (rs.getBoolean(index) ? Boolean.TRUE : Boolean.FALSE);
      }
   };

   public static final JDBCResultSetReader BYTE_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return new Byte(rs.getByte(index));
      }
   };

   public static final JDBCResultSetReader CHARACTER_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return rs.getString(index);
      }

      protected Object coerceToJavaType(Object value, Class destination)
      {
         //
         // java.lang.String --> java.lang.Character or char
         //
         // just grab first character
         if(value instanceof String && (destination == Character.class || destination == Character.TYPE))
         {
            return new Character(((String)value).charAt(0));
         }
         else
         {
            return value;
         }
      }
   };

   public static final JDBCResultSetReader SHORT_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return new Short(rs.getShort(index));
      }
   };

   public static final JDBCResultSetReader INT_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return new Integer(rs.getInt(index));
      }
   };

   public static final JDBCResultSetReader LONG_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return new Long(rs.getLong(index));
      }
   };

   public static final JDBCResultSetReader FLOAT_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return new Float(rs.getFloat(index));
      }
   };

   public static final JDBCResultSetReader DOUBLE_READER = new AbstractPrimitiveReader()
   {
      protected Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException
      {
         return new Double(rs.getDouble(index));
      }
   };

   abstract class AbstractResultSetReader implements JDBCResultSetReader
   {
      public Object get(ResultSet rs, int index, Class destination, Logger log) throws SQLException
      {
         Object result = readResult(rs, index, destination);
         if(result != null)
            result = coerceToJavaType(result, destination);

         if(log.isTraceEnabled())
         {
            log.trace("result: i=" + index + ", type=" + destination.getName() + ", value=" + result);
         }

         return result;
      }

      protected abstract Object readResult(ResultSet rs, int index, Class destination)
         throws SQLException;

      protected Object coerceToJavaType(Object value, Class destination)
         throws SQLException
      {
         try
         {
            //
            // java.rmi.MarshalledObject
            //
            // get unmarshalled value
            if(value instanceof MarshalledObject && !destination.equals(MarshalledObject.class))
            {
               value = ((MarshalledObject)value).get();
            }

            //
            // javax.ejb.Handle
            //
            // get the object back from the handle
            if(value instanceof Handle)
            {
               value = ((Handle)value).getEJBObject();
            }

            // Did we get the desired result?
            if(destination.isAssignableFrom(value.getClass()))
            {
               return value;
            }

            if(destination == java.math.BigInteger.class && value.getClass() == java.math.BigDecimal.class)
            {
               return ((java.math.BigDecimal)value).toBigInteger();
            }

            // oops got the wrong type - nothing we can do
            throw new SQLException("Got a " + value.getClass().getName() + "[cl=" +
               System.identityHashCode(value.getClass().getClassLoader()) +
               ", value=" + value + "] while looking for a " +
               destination.getName() + "[cl=" +
               System.identityHashCode(destination) + "]");
         }
         catch(RemoteException e)
         {
            throw new SQLException("Unable to load EJBObject back from Handle: " + e);
         }
         catch(IOException e)
         {
            throw new SQLException("Unable to load to deserialize result: " + e);
         }
         catch(ClassNotFoundException e)
         {
            throw new SQLException("Unable to load to deserialize result: " + e);
         }
      }
   }
}
