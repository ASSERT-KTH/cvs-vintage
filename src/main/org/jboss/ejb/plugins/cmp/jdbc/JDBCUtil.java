/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.rmi.MarshalledObject;
import java.rmi.RemoteException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.Map;
import java.util.HashMap;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;

/**
 * JDBCUtil takes care of some of the more anoying JDBC tasks.
 * It hanles safe closing of jdbc resources, setting statement
 * parameters and loading query results.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.13 $
 */
public final class JDBCUtil
{
   private static Logger log = Logger.getLogger(JDBCUtil.class.getName());

   public static void safeClose(Connection con)
   {
      if(con != null)
      {
         try
         {
            con.close();
         } catch(SQLException e)
         {
            log.error("SQL error", e);
         }
      }
   }
   
   public static void safeClose(ResultSet rs)
   {
      if(rs != null)
      {
         try
         {
            rs.close();
         } catch(SQLException e)
         {
            log.error("SQL error", e);
         }
      }
   }
   
   public static void safeClose(Statement statement)
   {
      if(statement != null)
      {
         try
         {
            statement.close();
         } catch(SQLException e)
         {
            log.error("SQL error", e);
         }
      }
   }
   
   public static void safeClose(InputStream in)
   {
      if(in != null)
      {
         try
         {
            in.close();
         } catch(IOException e)
         {
            log.error("SQL error", e);
         }
      }
   }
   
   public static void safeClose(OutputStream out)
   {
      if(out != null)
      {
         try
         {
            out.close();
         } catch(IOException e)
         {
            log.error("SQL error", e);
         }
      }
   }
   
   public static void safeClose(Reader reader)
   {
      if(reader != null)
      {
         try
         {
            reader.close();
         } catch(IOException e)
         {
            log.error("SQL error", e);
         }
      }
   }
   
   public static void safeClose(Writer writter)
   {
      if(writter != null)
      {
         try
         {
            writter.close();
         } catch(IOException e)
         {
            log.error("SQL error", e);
         }
      }
   }
    
   /**
    * Sets a parameter in this Command's PreparedStatement.
    * Handles null values, and provides tracing.
    *
    * @param ps the PreparedStatement whose parameter needs to be set.
    * @param index the index (1-based) of the parameter to be set.
    * @param jdbcType the JDBC type of the parameter.
    * @param value the value which the parameter is to be set to.
    * @throws SQLException if parameter setting fails.
    */
   public static void setParameter(
         Logger log, 
         PreparedStatement ps, 
         int index, 
         int jdbcType, 
         Object value) throws SQLException
   {
      if(log.isTraceEnabled()) {
         log.trace("Set parameter: " +
               "index=" + index + ", " +
               "jdbcType=" + getJDBCTypeName(jdbcType) + ", " +
               "value=" + ((value == null) ? "NULL" : value));
      }
      
      //
      // null
      //  
      if (value == null)
      {
         ps.setNull(index, jdbcType);
         return;
      }
      
      // 
      // coerce parameter into correct SQL type 
      // (for DATE, TIME, TIMESTAMP, CHAR)
      //
      value = coerceToSQLType(jdbcType, value);
         
      //
      // handle CLOBs special
      //
      // This won't fix the Oracle problem with CLOBs > 2000 characters,
      // but it is a step in the right direction.
      if(jdbcType == Types.CLOB) 
      {
         String string = value.toString();
         StringReader reader = null;
         try {
            reader = new StringReader(string);
            ps.setCharacterStream(index, reader, string.length());
         } finally
         {
            safeClose(reader);
         }
         return;
      } 

      //
      // Binary types need to be converted to a byte array and set
      //
      if(isBinaryJDBCType(jdbcType))
      {
         byte[] bytes = convertObjectToByteArray(value);

         if(jdbcType == Types.BLOB) 
         {
            // if the jdbc type is blob use a real blob
            ps.setBlob(index, new ByteArrayBlob(bytes));
         } else if (bytes.length < 2000)
         {
            // it's more efficient to use setBinaryStream for large
            // streams, and causes problems if not done on some DBMS
            // implementations
            ps.setBytes(index, bytes);
         } else
         {
            InputStream in = null;
            try
            {
               in = new ByteArrayInputStream(bytes);
               ps.setBinaryStream(index, in, bytes.length);
            } finally
            {
               safeClose(in);
            }
         }
         return;
      }

      //
      //  Standard SQL type
      //
      ps.setObject(index, value, jdbcType);
   }
   
   /**
    * Used for all retrieval of results from <code>ResultSet</code>s.
    * Implements tracing, and allows some tweaking of returned types.
    *
    * @param rs the <code>ResultSet</code> from which a result is 
    *    being retrieved.
    * @param index index of the result column.
    * @param destination The class of the variable this is going into
    */
   public static Object getResult(
         Logger log, 
         ResultSet rs, 
         int index, 
         Class destination) throws SQLException
   {
      boolean gotResult = false;
      Object value = null;
      
      //
      // Non-binary types
      //
      Method method = (Method)rsTypes.get(destination.getName());
      if(method != null)
      {
         try
         {
            value = method.invoke(rs, new Object[]{new Integer(index)});
            if(rs.wasNull())
            {
               value = null;
            }

            if(log.isTraceEnabled()) {
               log.trace("Get result: index=" + index + 
                     ", javaType=" + destination.getName() + 
                     ", Simple, value=" + value);
            }
            gotResult = true;
         } catch(IllegalAccessException e)
         {
            // Whatever, I guess non-binary will not work for this field.
         } catch(InvocationTargetException e)
         {
            // Whatever, I guess non-binary will not work for this field.
         }
      }
   
      //
      // Binary types
      //
      if(!gotResult) {
         // I guess this is binary data
         byte[] bytes = rs.getBytes(index);
         if( bytes == null )
         {
            return null;
         }
      
         // if we are not looking for a byte array convert the bytes into 
         // a real object; otherwise we are done
         if (destination != byte[].class)
         {
            value = convertByteArrayToObject(bytes);
         } else
         {
            value = bytes;
         }

         if(log.isTraceEnabled()) {
            log.trace("Get result: index=" + index + 
                  ", javaType=" + destination.getName() + 
                  ", Binary, value=" + value);
         }
         gotResult = true;
      }

      //
      // Coerce result into correct java type
      //
      return coerceToJavaType(value, destination);
   }
   
   private static Object coerceToJavaType(
         Object value, 
         Class destination)throws SQLException
   {
      try
      {
         //
         // null
         //
         if(value == null) {
            return null;
         }
         
         // 
         // java.rmi.MarshalledObject
         //
         // get unmarshalled value
         if(value instanceof MarshalledObject && 
               !destination.equals(MarshalledObject.class))
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
         
         // 
         // Primitive wrapper classes
         //
         // We have a primitive wrapper and we want a real primitive
         // just return the wrapper and the vm will convert it at the proxy
         if(destination.isPrimitive())
         {
            if((destination.equals(Byte.TYPE) && value instanceof Byte) ||
            (destination.equals(Short.TYPE) && value instanceof Short) ||
            (destination.equals(Character.TYPE) && 
                  value instanceof Character) ||
            (destination.equals(Boolean.TYPE) && value instanceof Boolean) ||
            (destination.equals(Integer.TYPE) && value instanceof Integer) ||
            (destination.equals(Long.TYPE) && value instanceof Long) ||
            (destination.equals(Float.TYPE) && value instanceof Float) ||
            (destination.equals(Double.TYPE) && value instanceof Double)
            )
            {
               return value;
            }
         }

         //
         // java.util.Date
         //
         // make new copy as sub types have problems in comparions
         if(destination == java.util.Date.class && 
               value instanceof java.util.Date) 
         {
            // handle timestamp special becauses it hoses the milisecond values
            if(value instanceof java.sql.Timestamp) {
               java.sql.Timestamp ts = (java.sql.Timestamp)value;
               
               // Timestamp returns whole seconds from getTime and partial 
               // seconds are retrieved from getNanos()
               // Adrian Brock: Not in 1.4 it doesn't
               long temp = ts.getTime();
               if (temp % 1000 == 0)
                  temp += ts.getNanos()/1000000;
               return new java.util.Date(temp);
            } else 
            {
               return new java.util.Date(((java.util.Date)value).getTime());
            }
         } 
         
         //
         // java.sql.Time
         //
         // make a new copy object; you never know what a driver will return
         if(destination == java.sql.Time.class &&
               value instanceof java.sql.Time) {
            return new java.sql.Time(((java.sql.Time)value).getTime());
         } 
         
         //
         // java.sql.Date
         //
         // make a new copy object; you never know what a driver will return
         if(destination == java.sql.Date.class &&
               value instanceof java.sql.Date) {
   
            return new java.sql.Date(((java.sql.Date)value).getTime());
         } 

         //
         // java.sql.Timestamp
         //
         // make a new copy object; you never know what a driver will return
         if(destination == java.sql.Timestamp.class &&
               value instanceof java.sql.Timestamp) {
   
            // make a new Timestamp object; you never know 
            // what a driver will return
            java.sql.Timestamp orignal = (java.sql.Timestamp)value;
            java.sql.Timestamp copy = new java.sql.Timestamp(orignal.getTime());
            copy.setNanos(orignal.getNanos());
            return copy;
         } 

         //
         // java.lang.String --> java.lang.Character or char
         //
         // just grab first character
         if(value instanceof String &&
               (destination == Character.class || 
                destination == Character.TYPE) )
         {
            return new Character(((String)value).charAt(0));
         }
    
         // Did we get the desired result?
         if(destination.isAssignableFrom(value.getClass()))
         {
            return value;
         }
         
         // oops got the wrong type - nothing we can do
         throw new SQLException("Got a " + value.getClass().getName() + "[cl=" +
               System.identityHashCode(value.getClass().getClassLoader()) +
               ", value=" + value + "] while looking for a " + 
               destination.getName() + "[cl=" +
               System.identityHashCode(destination) + "]");
               

      } catch (RemoteException e)
      {
         throw new SQLException("Unable to load EJBObject back from Handle: "
               + e);
      } catch(IOException e)
      {
         throw new SQLException("Unable to load to deserialize result: "+e);
      } catch (ClassNotFoundException e)
      {
         throw new SQLException("Unable to load to deserialize result: "+e);
      }
   }
   
   /**
    * Returns true if the JDBC type should be (de-)serialized as a
    * binary stream and false otherwise.
    *
    * @param jdbcType the JDBC type
    * @return true if binary type, false otherwise
    */
   public static boolean isBinaryJDBCType(int jdbcType)
   {
      return (Types.BINARY == jdbcType ||
            Types.BLOB == jdbcType ||
            Types.JAVA_OBJECT == jdbcType ||
            Types.LONGVARBINARY == jdbcType ||
            Types.OTHER == jdbcType ||
            Types.STRUCT == jdbcType ||
            Types.VARBINARY == jdbcType);
   }
   
   /**
    * Coerces the input value into the correct type for the specified
    * jdbcType.
    * 
    * @param jdbcType the jdbc type to which the value will be assigned
    * @param value the value to coerce
    * @return the corrected object
    */
   private static Object coerceToSQLType(int jdbcType, Object value)
   {
      if(value.getClass() == java.util.Date.class)
      {
         if(jdbcType == Types.DATE)
         {
            return new java.sql.Date(((java.util.Date)value).getTime());
         } else if(jdbcType == Types.TIME)
         {
            return new java.sql.Time(((java.util.Date)value).getTime());
         } else if(jdbcType == Types.TIMESTAMP)
         {
            return new java.sql.Timestamp(((java.util.Date)value).getTime());
         }
      } else if(value.getClass() == Character.class && 
            jdbcType == Types.VARCHAR)
      {
         value = value.toString();
      }
      return value;
   }

   /**
    * Coverts the value into a byte array.
    * @param value the value to convert into a byte array
    * @return the byte representation of the value
    * @throws SQLException if a problem occures in the conversion
    */
   private static byte[] convertObjectToByteArray(Object value) 
         throws SQLException
   {
      // Do we already have a byte array?
      if (value instanceof byte[])
      {
         return (byte[])value;
      }
      
      ByteArrayOutputStream baos = null;
      ObjectOutputStream oos = null;
      try
      {
         // ejb-reference: store the handle
         if (value instanceof EJBObject)
         {
            value = ((EJBObject)value).getHandle();
         }
         
         // Marshall the object using MashalledValue to handle classloaders 
         value = new MarshalledValue(value);
         
         // return the serialize the value
         baos = new ByteArrayOutputStream();
         oos = new ObjectOutputStream(baos);
         oos.writeObject(value);
         return baos.toByteArray();
      } catch(RemoteException e)
      {
         throw new SQLException("Cannot get Handle of EJBObject: "+e);
      } catch(IOException e)
      {
         throw new SQLException("Can't serialize binary object: " + e);
      } finally
      {
         safeClose(oos);
         safeClose(baos);
      }
   }
   
   /**
    * Coverts the byte array into an object.
    * @param bytes the bytes to convert
    * @param destination the desired resultant type
    * @return the object repsentation of the byte array
    * @throws SQLException if a problem occures in the conversion
    */
   private static Object convertByteArrayToObject(byte[] bytes) 
         throws SQLException
   {
      ByteArrayInputStream bais = null;
      ObjectInputStream ois = null;
      try
      {
         Object value;
         
         // deserialize result
         bais = new ByteArrayInputStream(bytes);
         ois = new ObjectInputStream(bais);
         value = ois.readObject();
         
         // de-marshall value if possible
         if(value instanceof MarshalledValue)  {
            value = ((MarshalledValue)value).get();
         } else if(value instanceof MarshalledObject) {
            value = ((MarshalledObject)value).get();
         }
         
         // ejb-reference: get the object back from the handle
         if(value instanceof Handle)
         {
            value = ((Handle)value).getEJBObject();
         }
         return value;
      } catch (RemoteException e)
      {
         throw new SQLException("Unable to load EJBObject back from Handle: " 
               + e);
      } catch (IOException e)
      {
         throw new SQLException("Unable to load to deserialize result: "+e);
      } catch (ClassNotFoundException e)
      {
         throw new SQLException("Unable to load to deserialize result: "+e);
      } finally
      {
         safeClose(ois);
         safeClose(bais);
      }
   }
   
   //
   // Simple helper methods
   //
   private static Map jdbcTypeNames;
   private final static Map rsTypes;
   
   /**
    * Gets the JDBC type name corresponding to the given type code.
    * Only used in debug log messages.
    *
    * @param jdbcType the integer JDBC type code.
    * @return the JDBC type name.
    * @see Types
    */
   private static String getJDBCTypeName(int jdbcType)
   {
      return (String)jdbcTypeNames.get(new Integer(jdbcType));
   }
   
   static
   {
      // Initialize the mapping between non-binary java result set
      // types and the method on ResultSet that is used to retrieve
      // a value of the java type.
      rsTypes = new HashMap();
      Class[] arg = new Class[]
      {Integer.TYPE};
      try
      {
         // java.util.Date
         rsTypes.put(java.util.Date.class.getName(),
               ResultSet.class.getMethod("getTimestamp", arg));
         // java.sql.Date
         rsTypes.put(java.sql.Date.class.getName(),
               ResultSet.class.getMethod("getDate", arg));
         // Time
         rsTypes.put(java.sql.Time.class.getName(),
               ResultSet.class.getMethod("getTime", arg));
         // Timestamp
         rsTypes.put(java.sql.Timestamp.class.getName(),
               ResultSet.class.getMethod("getTimestamp", arg));
         // BigDecimal
         rsTypes.put(java.math.BigDecimal.class.getName(),
               ResultSet.class.getMethod("getBigDecimal", arg));
         // java.sql.Ref Does this really work?
         rsTypes.put(java.sql.Ref.class.getName(),
               ResultSet.class.getMethod("getRef", arg));
         // String
         rsTypes.put(java.lang.String.class.getName(),
               ResultSet.class.getMethod("getString", arg));
         // Boolean
         rsTypes.put(java.lang.Boolean.class.getName(),
               ResultSet.class.getMethod("getBoolean", arg));
         // boolean
         rsTypes.put(Boolean.TYPE.getName(),
               ResultSet.class.getMethod("getBoolean", arg));
         // Byte
         rsTypes.put(java.lang.Byte.class.getName(),
               ResultSet.class.getMethod("getByte", arg));
         // byte
         rsTypes.put(Byte.TYPE.getName(),
               ResultSet.class.getMethod("getByte", arg));
         // Character
         rsTypes.put(java.lang.Character.class.getName(),
               ResultSet.class.getMethod("getString", arg));
         // char
         rsTypes.put(Character.TYPE.getName(),
               ResultSet.class.getMethod("getString", arg));
         // Short
         rsTypes.put(java.lang.Short.class.getName(),
               ResultSet.class.getMethod("getShort", arg));
         // short
         rsTypes.put(Short.TYPE.getName(),
               ResultSet.class.getMethod("getShort", arg));
         // Integer
         rsTypes.put(java.lang.Integer.class.getName(),
               ResultSet.class.getMethod("getInt", arg));
         // int
         rsTypes.put(Integer.TYPE.getName(),
               ResultSet.class.getMethod("getInt", arg));
         // Long
         rsTypes.put(java.lang.Long.class.getName(),
               ResultSet.class.getMethod("getLong", arg));
         // long
         rsTypes.put(Long.TYPE.getName(),
               ResultSet.class.getMethod("getLong", arg));
         // Float
         rsTypes.put(java.lang.Float.class.getName(),
               ResultSet.class.getMethod("getFloat", arg));
         // float
         rsTypes.put(Float.TYPE.getName(),
               ResultSet.class.getMethod("getFloat", arg));
         // Double
         rsTypes.put(java.lang.Double.class.getName(),
               ResultSet.class.getMethod("getDouble", arg));
         // double
         rsTypes.put(Double.TYPE.getName(),
               ResultSet.class.getMethod("getDouble", arg));
         // byte[]
         rsTypes.put("[B",
               ResultSet.class.getMethod("getBytes", arg));
      } catch(NoSuchMethodException e)
      {
         // Should never happen
         log.error("SQL error", e);
      }
      
      // Initializes the map between jdbcType (int) and the name of the type.
      // This map is used to print meaningful debug and error messages.
      jdbcTypeNames = new HashMap();
      Field[] fields = Types.class.getFields();
      for(int i = 0; i < fields.length; i++)
      {
         try
         {
            jdbcTypeNames.put(fields[i].get(null), fields[i].getName());
         } catch (IllegalAccessException e)
         {
            // Should never happen
            log.error("SQL error", e);
         }
      }
   }
   
}
