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

import org.jboss.logging.Logger;

/**
 * JDBCUtil takes care of some of the more anoying JDBC tasks.
 * It hanles safe closing of jdbc resources, setting statement
 * parameters and loading query results.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.10 $
 */
public class JDBCUtil
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
      
      if (value == null)
      {
         ps.setNull(index, jdbcType);
      } else
      {
         // convert to valid SQL date types (for DATE, TIME, TIMESTAMP)
         value = convertToSQLDate(jdbcType, value);
         
         if(isBinaryJDBCType(jdbcType))
         {
            byte[] bytes = convertObjectToByteArray(value);
            setBinaryParameter(ps, index, jdbcType, bytes);
         } else
         {
            ps.setObject(index, value, jdbcType);
         }
      }
   }
   
   private static void setBinaryParameter(
         PreparedStatement ps, 
         int index,
         int jdbcType,
         byte[] bytes) throws SQLException
   {
      // it's more efficient to use setBinaryStream for large
      // streams, and causes problems if not done on some DBMS
      // implementations
      if(jdbcType == Types.BLOB) 
      {
         // if the jdbc type is blob use a blob
         ps.setBlob(index, new ByteArrayBlob(bytes));
      } else if (bytes.length < 2000)
      {
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
      Object[] returnValue = new Object[1];
      if(getNonBinaryResult(rs, index, destination, returnValue))
      {
         if(log.isTraceEnabled()) {
            log.trace("Get result: index=" + index + 
                  ", javaType=" + destination.getName() + 
                  ", Simple, value=" + returnValue[0]);
         }
      } else if(getBinaryResult(rs, index, destination, returnValue))
      {
         if(log.isTraceEnabled()) {
            log.trace("Get result: index=" + index + 
                  ", javaType=" + destination.getName() + 
                  ", Binary, value=" + returnValue[0]);
         }
      } else {
         throw new SQLException("Unable to load a ResultSet column into " +
               "a variable of type '" + destination.getName() + "'");
      }

      // convert to valid from SQL date back to Java date 
      // fixes comparisons of date types
      returnValue[0] = convertToJavaDate(destination, returnValue[0]);

      return returnValue[0];
   }
   
   private static boolean getNonBinaryResult(
         ResultSet rs, 
         int index, 
         Class destination, 
         Object returnValue[]) throws SQLException
   {
      Method method = (Method)rsTypes.get(destination.getName());
      if(method != null)
      {
         try
         {
            Object value = method.invoke(rs, new Object[]{new Integer(index)});
            if(rs.wasNull())
            {
               returnValue[0] = null;
            } else
            {
               if(value instanceof String &&
                     (destination.isAssignableFrom(Character.class) ||
                     destination.isAssignableFrom(Character.TYPE) ))
               {
                  value = new Character(((String)value).charAt(0));
               }
               returnValue[0] = value;
            }
            return true;
         } catch(IllegalAccessException e)
         {
            // What-ever, I guess non-binary will not work for this field.
         } catch(InvocationTargetException e)
         {
            // What-ever, I guess non-binary will not work for this field.
         }
      }
      return false;
   }
   
   private static boolean getBinaryResult(
         ResultSet rs, 
         int index, 
         Class destination, 
         Object returnValue[]) throws SQLException
   {
      byte[] bytes = rs.getBytes(index);
      if( bytes == null )
      {
         returnValue[0] = null;
         return true;
      }
      
      Object value = convertByteArrayToObject(bytes, destination);
      return convertToJavaType(value, destination, returnValue);
   }
   
   private static boolean convertToJavaType(
         Object value, 
         Class destination, 
         Object returnValue[]) throws SQLException
   {
      try
      {
         // Was the object double marshalled?
         if(value instanceof MarshalledObject && 
               !destination.equals(MarshalledObject.class))
         {
            value = ((MarshalledObject)value).get();
         }
         
         // ejb-reference: get the object back from the handle
         if(value instanceof Handle)
         {
            value = ((Handle)value).getEJBObject();
            if(destination.isAssignableFrom(value.getClass()))
            {
               returnValue[0] = value;
               return true;
            } else
            {
               throw new SQLException("Got a " + value.getClass().getName() + 
                     ": '" + value + "' while looking for a " + 
                     destination.getName());
            }
         }
         
         // Are we done yet?
         if(destination.isAssignableFrom(value.getClass()))
         {
            returnValue[0] = value;
            return true;
         }
         
         // Ok is this a primitive wrapper
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
               returnValue[0] = value;
               return true;
            }
         }
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
      return false;
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
      Types.CLOB == jdbcType ||
      Types.JAVA_OBJECT == jdbcType ||
      Types.LONGVARBINARY == jdbcType ||
      Types.OTHER == jdbcType ||
      Types.STRUCT == jdbcType ||
      Types.VARBINARY == jdbcType);
   }
   
   private static Object convertToSQLDate(int jdbcType, Object value)
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
      }
      return value;
   }

   private static Object convertToJavaDate(Class destination, Object value)
   {
      // make a real java.util.Date (sub types have problems with comparions)
      if(destination == java.util.Date.class && 
            value instanceof java.util.Date) 
      {
         // handle timestamp special becauses it hoses the milisecond values
         if(value instanceof java.sql.Timestamp) {
            java.sql.Timestamp ts = (java.sql.Timestamp)value;
            
            // Timestamp returns whole seconds from getTime and partial 
            // seconds are retrieved from getNanos()
            return new java.util.Date(ts.getTime() + (ts.getNanos()/1000000));
         } else 
         {
            return new java.util.Date(((java.util.Date)value).getTime());
         }
      } else if(destination == java.sql.Time.class &&
            value instanceof java.sql.Time) {

         // make a new Time object; you never know what a driver will return
         return new java.sql.Time(((java.sql.Time)value).getTime());
      } else if(destination == java.sql.Date.class &&
            value instanceof java.sql.Date) {

         // make a new Date object; you never know what a driver will return
         return new java.sql.Date(((java.sql.Date)value).getTime());
      } else if(destination == java.sql.Timestamp.class &&
            value instanceof java.sql.Timestamp) {

         // make a new Timestamp object; you never know 
         // what a driver will return
         java.sql.Timestamp in = (java.sql.Timestamp)value;
         java.sql.Timestamp out = new java.sql.Timestamp(in.getTime());
         out.setNanos(in.getNanos());
         return out;
      }
      return value;
   }
    
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
         
         // return the serialize the value
         baos = new ByteArrayOutputStream();
         oos = new ObjectOutputStream(baos);
         
         // Marshall the object and write it
         oos.writeObject(new MarshalledObject(value));
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
   
   private static Object convertByteArrayToObject(
         byte[] bytes, 
         Class destination) throws SQLException
   {
      // Are we looking for a byte array
      if (destination == byte[].class)
      {
         return bytes;
      }

      ByteArrayInputStream bais = null;
      ObjectInputStream ois = null;
      try
      {
         Object value;
         
         // deserialize result
         bais = new ByteArrayInputStream(bytes);
         ois = new ObjectInputStream(bais);
         value = ois.readObject();
         
         // de-marshall value
         value = ((MarshalledObject)value).get();
         
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
