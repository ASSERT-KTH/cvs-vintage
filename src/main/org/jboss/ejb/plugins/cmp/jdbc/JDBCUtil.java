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
 * @version $Revision: 1.8 $
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
   public static void setParameter(Logger log, PreparedStatement ps, int index, int jdbcType, Object value) throws SQLException
   {
      if(log.isDebugEnabled()) {
         log.debug("Set parameter: " +
               "index=" + index + ", " +
               "jdbcType=" + getJDBCTypeName(jdbcType) + ", " +
               "value=" + ((value == null) ? "NULL" : value));
      }
      
      if (value == null)
      {
         ps.setNull(index, jdbcType);
      } else
      {
         // convert to valid SQL data types (for DATE, TIME, TIMESTAMP)
         value = convertToSQLType(jdbcType, value);
         
         if(isBinaryJDBCType(jdbcType))
         {
            byte[] bytes = convertObjectToByteArray(value);
            setBinaryParameter(ps, index, bytes);
         } else
         {
            ps.setObject(index, value, jdbcType);
         }
      }
   }
   
   private static void setBinaryParameter(PreparedStatement ps, int index, byte[] bytes) throws SQLException
   {
      // it's more efficient to use setBinaryStream for large
      // streams, and causes problems if not done on some DBMS
      // implementations
      if (bytes.length < 2000)
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
    * @param rs the <code>ResultSet</code> from which a result is being retrieved.
    * @param index index of the result column.
    * @param destination The class of the variable this is going into
    */
   public static Object getResult(Logger log, ResultSet rs, int index, Class destination) throws SQLException
   {
      Object[] returnValue = new Object[1];
      if(getNonBinaryResult(rs, index, destination, returnValue))
      {
         if(log.isDebugEnabled()) {
            log.debug("Get result: index=" + index + 
                  ", javaType=" + destination.getName() + 
                  ", Simple, value=" + returnValue[0]);
         }
         return returnValue[0];
      } else if(getObjectResult(rs, index, destination, returnValue))
      {
         if(log.isDebugEnabled()) {
            log.debug("Get result: index=" + index + 
                  ", javaType=" + destination.getName() + 
                  ", Object, value=" + returnValue[0]);
         }
         return returnValue[0];
      } else if(getBinaryResult(rs, index, destination, returnValue))
      {
         if(log.isDebugEnabled()) {
            log.debug("Get result: index=" + index + 
                  ", javaType=" + destination.getName() + 
                  ", Binary, value=" + returnValue[0]);
         }
         return returnValue[0];
      }
      throw new SQLException("Unable to load a ResultSet column into a variable of type '" + destination.getName() + "'");
   }
   
   private static boolean getNonBinaryResult(ResultSet rs, int index, Class destination, Object returnValue[]) throws SQLException
   {
      Method method = (Method)rsTypes.get(destination.getName());
      if(method != null)
      {
         try
         {
            Object value = method.invoke(rs, new Object[]
            {new Integer(index)});
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
   
   private static boolean getObjectResult(ResultSet rs, int index, Class destination, Object returnValue[]) throws SQLException
   {
      //
      // I think this method is very dangerous, and we should consider removing it.
      // Some lesser databases only allow you to read a column once and if the
      // object based stratege fails, we have to read the column again with getBytes.
      Object value = rs.getObject(index);
      if(value == null)
      {
         returnValue[0] = null;
         return true;
      }
      
      return convertToJavaType(value, destination, returnValue);
   }
   
   private static boolean getBinaryResult(ResultSet rs, int index, Class destination, Object returnValue[]) throws SQLException
   {
      byte[] bytes = rs.getBytes(index);
      if( bytes == null )
      {
         returnValue[0] = null;
         return true;
      }
      
      Object value = convertByteArrayToObject(bytes);
      return convertToJavaType(value, destination, returnValue);
   }
   
   private static boolean convertToJavaType(Object value, Class destination, Object returnValue[]) throws SQLException
   {
      try
      {
         // Was the object double marshalled?
         if(value instanceof MarshalledObject && !destination.equals(MarshalledObject.class))
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
               throw new SQLException("Got a " + value.getClass().getName() + ": '" + value + "' while looking for a " + destination.getName());
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
            (destination.equals(Character.TYPE) && value instanceof Character) ||
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
         throw new SQLException("Unable to load EJBObject back from Handle: " +e);
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
   
   private static Object convertToSQLType(int jdbcType, Object value)
   {
      if(jdbcType == Types.DATE)
      {
         if(value.getClass().getName().equals("java.util.Date"))
         {
            return new java.sql.Date(((java.util.Date)value).getTime());
         }
      } else if(jdbcType == Types.TIME)
      {
         if(value.getClass().getName().equals("java.util.Date"))
         {
            return new java.sql.Time(((java.util.Date)value).getTime());
         }
      } else if(jdbcType == Types.TIMESTAMP)
      {
         if(value.getClass().getName().equals("java.util.Date"))
         {
            return new java.sql.Timestamp(((java.util.Date)value).getTime());
         }
      }
      return value;
   }
   
   private static byte[] convertObjectToByteArray(Object value) throws SQLException
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
   
   private static Object convertByteArrayToObject(byte[] bytes) throws SQLException
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
         throw new SQLException("Unable to load EJBObject back from Handle: " +e);
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
         rsTypes.put(java.util.Date.class.getName(),       ResultSet.class.getMethod("getTimestamp", arg));
         rsTypes.put(java.sql.Date.class.getName(),        ResultSet.class.getMethod("getDate", arg));
         rsTypes.put(java.sql.Time.class.getName(),        ResultSet.class.getMethod("getTime", arg));
         rsTypes.put(java.sql.Timestamp.class.getName(),   ResultSet.class.getMethod("getTimestamp", arg));
         rsTypes.put(java.math.BigDecimal.class.getName(), ResultSet.class.getMethod("getBigDecimal", arg));
         rsTypes.put(java.sql.Ref.class.getName(),         ResultSet.class.getMethod("getRef", arg));
         rsTypes.put(java.lang.String.class.getName(),     ResultSet.class.getMethod("getString", arg));
         rsTypes.put(java.lang.Boolean.class.getName(),    ResultSet.class.getMethod("getBoolean", arg));
         rsTypes.put(Boolean.TYPE.getName(),               ResultSet.class.getMethod("getBoolean", arg));
         rsTypes.put(java.lang.Byte.class.getName(),       ResultSet.class.getMethod("getByte", arg));
         rsTypes.put(Byte.TYPE.getName(),                  ResultSet.class.getMethod("getByte", arg));
         rsTypes.put(java.lang.Character.class.getName(),  ResultSet.class.getMethod("getString", arg));
         rsTypes.put(Character.TYPE.getName(),             ResultSet.class.getMethod("getString", arg));
         rsTypes.put(java.lang.Short.class.getName(),      ResultSet.class.getMethod("getShort", arg));
         rsTypes.put(Short.TYPE.getName(),                 ResultSet.class.getMethod("getShort", arg));
         rsTypes.put(java.lang.Integer.class.getName(),    ResultSet.class.getMethod("getInt", arg));
         rsTypes.put(Integer.TYPE.getName(),               ResultSet.class.getMethod("getInt", arg));
         rsTypes.put(java.lang.Long.class.getName(),       ResultSet.class.getMethod("getLong", arg));
         rsTypes.put(Long.TYPE.getName(),                  ResultSet.class.getMethod("getLong", arg));
         rsTypes.put(java.lang.Float.class.getName(),      ResultSet.class.getMethod("getFloat", arg));
         rsTypes.put(Float.TYPE.getName(),                 ResultSet.class.getMethod("getFloat", arg));
         rsTypes.put(java.lang.Double.class.getName(),     ResultSet.class.getMethod("getDouble", arg));
         rsTypes.put(Double.TYPE.getName(),                ResultSet.class.getMethod("getDouble", arg));
         // byte[]
         rsTypes.put("[B",                                 ResultSet.class.getMethod("getBytes", arg));
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
