/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.math.BigDecimal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.rmi.RemoteException;
import java.rmi.MarshalledObject;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

import javax.sql.DataSource;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.metadata.JawsEntityMetaData;
import org.jboss.ejb.plugins.jaws.metadata.CMPFieldMetaData;
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;
import org.jboss.logging.Log;
import org.jboss.logging.Logger;

/**
 * Abstract superclass for all JAWS Commands that use JDBC directly.
 * Provides a Template Method for jdbcExecute(), default implementations
 * for some of the methods called by this template, and a bunch of
 * utility methods that database commands may need to call.
 *
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.14 $
 */
public abstract class JDBCCommand
{
    private final static HashMap rsTypes = new HashMap();
    static {
        Class[] arg = new Class[]{Integer.TYPE};
        try {
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
            rsTypes.put(java.lang.Double.class.getName(),     ResultSet.class.getMethod("getDouble", arg));
            rsTypes.put(Double.TYPE.getName(),                ResultSet.class.getMethod("getDouble", arg));
            rsTypes.put(java.lang.Float.class.getName(),      ResultSet.class.getMethod("getFloat", arg));
            rsTypes.put(Float.TYPE.getName(),                 ResultSet.class.getMethod("getFloat", arg));
            rsTypes.put(java.lang.Integer.class.getName(),    ResultSet.class.getMethod("getInt", arg));
            rsTypes.put(Integer.TYPE.getName(),               ResultSet.class.getMethod("getInt", arg));
            rsTypes.put(java.lang.Long.class.getName(),       ResultSet.class.getMethod("getLong", arg));
            rsTypes.put(Long.TYPE.getName(),                  ResultSet.class.getMethod("getLong", arg));
            rsTypes.put(java.lang.Short.class.getName(),      ResultSet.class.getMethod("getShort", arg));
            rsTypes.put(Short.TYPE.getName(),                 ResultSet.class.getMethod("getShort", arg));
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

   // Attributes ----------------------------------------------------

   protected JDBCCommandFactory factory;
   protected JawsEntityMetaData jawsEntity;
   protected Log log;
   protected String name;    // Command name, used for debug trace

   private String sql;
   private static Map jdbcTypeNames;

   /**
    * Gives compile-time control of tracing.
    */
   public static boolean debug = false;

   // Constructors --------------------------------------------------

   /**
    * Construct a JDBCCommand with given factory and name.
    *
    * @param factory the factory which was used to create this JDBCCommand,
    *  which is also used as a common repository, shared by all an
    *  entity's Commands.
    * @param name the name to be used when tracing execution.
    */
   protected JDBCCommand(JDBCCommandFactory factory, String name)
   {
      this.factory = factory;
      this.jawsEntity = factory.getMetaData();
      this.log = factory.getLog();
      this.name = name;
   }

   // Protected -----------------------------------------------------

   /**
    * Template method handling the mundane business of opening
    * a database connection, preparing a statement, setting its parameters,
    * executing the prepared statement, handling the result,
    * and cleaning up.
    *
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method, and passed on to 'hook' methods for
    *  getting SQL and for setting parameters.
    * @return any result produced by the handling of the result of executing
    *  the prepared statement.
    * @throws Exception if connection fails, or if any 'hook' method
    *  throws an exception.
    */
   protected Object jdbcExecute(Object argOrArgs) throws Exception
   {
      Connection con = null;
      PreparedStatement stmt = null;
      Object result = null;

      try
      {
         con = getConnection();
         String theSQL = getSQL(argOrArgs);
         if (debug)
         {
            log.debug(name + " command executing: " + theSQL);
         }
         stmt = con.prepareStatement(theSQL);
         setParameters(stmt, argOrArgs);
         result = executeStatementAndHandleResult(stmt, argOrArgs);
      } catch(SQLException e) {
          log.exception(e);
          throw e;
      } finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            } catch (SQLException e)
            {
               Logger.exception(e);
            }
         }
         if (con != null)
         {
            try
            {
               con.close();
            } catch (SQLException e)
            {
               Logger.exception(e);
            }
         }
      }

      return result;
   }

   /**
    * Used to set static SQL in subclass constructors.
    *
    * @param sql the static SQL to be used by this Command.
    */
   protected void setSQL(String sql)
   {
      if (debug)
      {
         log.debug(name + " SQL: " + sql);
      }
      this.sql = sql;
   }

   /**
    * Gets the SQL to be used in the PreparedStatement.
    * The default implementation returns the <code>sql</code> field value.
    * This is appropriate in all cases where static SQL can be
    * constructed in the Command constructor.
    * Override if dynamically-generated SQL, based on the arguments
    * given to execute(), is needed.
    *
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return the SQL to use in the PreparedStatement.
    * @throws Exception if an attempt to generate dynamic SQL results in
    *  an Exception.
    */
   protected String getSQL(Object argOrArgs) throws Exception
   {
      return sql;
   }

   /**
    * Default implementation does nothing.
    * Override if parameters need to be set.
    *
    * @param stmt the PreparedStatement which will be executed by this Command.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @throws Exception if parameter setting fails.
    */
   protected void setParameters(PreparedStatement stmt, Object argOrArgs)
      throws Exception
   {
   }

   /**
    * Executes the PreparedStatement and handles result of successful execution.
    * This is implemented in subclasses for queries and updates.
    *
    * @param stmt the PreparedStatement to execute.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return any result produced by the handling of the result of executing
    *  the prepared statement.
    * @throws Exception if execution or result handling fails.
    */
   protected abstract Object executeStatementAndHandleResult(
            PreparedStatement stmt,
            Object argOrArgs) throws Exception;

   // ---------- Utility methods for use in subclasses ----------

   /**
    * Sets a parameter in this Command's PreparedStatement.
    * Handles null values, and provides tracing.
    *
    * @param stmt the PreparedStatement whose parameter needs to be set.
    * @param idx the index (1-based) of the parameter to be set.
    * @param jdbcType the JDBC type of the parameter.
    * @param value the value which the parameter is to be set to.
    * @throws SQLException if parameter setting fails.
    */
   protected void setParameter(PreparedStatement stmt,
                               int idx,
                               int jdbcType,
                               Object value)
      throws SQLException
   {
      if (debug)
      {
         log.debug("Set parameter: idx=" + idx +
                   ", jdbcType=" + getJDBCTypeName(jdbcType) +
                   ", value=" +
                   ((value == null) ? "NULL" : value));
      }

      if (value == null) {
         stmt.setNull(idx, jdbcType);
      } else {
          if(jdbcType == Types.DATE) {
              if(value.getClass().getName().equals("java.util.Date"))
                  value = new java.sql.Date(((java.util.Date)value).getTime());
          } else if(jdbcType == Types.TIME) {
              if(value.getClass().getName().equals("java.util.Date"))
                  value = new java.sql.Time(((java.util.Date)value).getTime());
          } else if(jdbcType == Types.TIMESTAMP) {
              if(value.getClass().getName().equals("java.util.Date"))
                  value = new java.sql.Timestamp(((java.util.Date)value).getTime());
          }			 
          if (jdbcType == Types.JAVA_OBJECT) {
              
			  // ejb-reference: store the handle
			  if (value instanceof EJBObject) try {
			  	 value = ((EJBObject)value).getHandle();
			  } catch (RemoteException e) {
				 throw new SQLException("Cannot get Handle of EJBObject: "+e);
			  }
			  
			  ByteArrayOutputStream baos = new ByteArrayOutputStream();

              try {
                  ObjectOutputStream oos = new ObjectOutputStream(baos);

                  oos.writeObject(new MarshalledObject(value));

                  oos.close();

              } catch (IOException e) {
                  throw new SQLException("Can't write Java object type to DB: " + e);
              }
              byte[] bytes = baos.toByteArray();
              stmt.setBytes(idx, bytes);
          } else {
              stmt.setObject(idx, value, jdbcType);
          }
      }
   }

   /**
    * Sets the PreparedStatement parameters for a primary key
    * in a WHERE clause.
    *
    * @param stmt the PreparedStatement
    * @param parameterIndex the index (1-based) of the first parameter to set
    * in the PreparedStatement
    * @param id the entity's ID
    * @return the index of the next unset parameter
    * @throws SQLException if parameter setting fails
    * @throws IllegalAccessException if accessing a field in the PK class fails
    */
   protected int setPrimaryKeyParameters(PreparedStatement stmt,
                                         int parameterIndex,
                                         Object id)
      throws IllegalAccessException, SQLException
   {
      Iterator it = jawsEntity.getPkFields();
      
      if (jawsEntity.hasCompositeKey())
      {
         while (it.hasNext())
         {
            PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
            int jdbcType = pkFieldMetaData.getJDBCType();
            Object value = getPkFieldValue(id, pkFieldMetaData);
            setParameter(stmt, parameterIndex++, jdbcType, value);
         }
      } else
      {
         PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
         int jdbcType = pkFieldMetaData.getJDBCType();
         setParameter(stmt, parameterIndex++, jdbcType, id);
      }

      return parameterIndex;
   }


   /**
    * Used for all retrieval of results from <code>ResultSet</code>s.
    * Implements tracing, and allows some tweaking of returned types.
    *
    * @param rs the <code>ResultSet</code> from which a result is being retrieved.
    * @param idx index of the result column.
    * @param destination The class of the variable this is going into
    */
    protected Object getResultObject(ResultSet rs, int idx, Class destination)
        throws SQLException{

        Object result = null;

        Method method = (Method)rsTypes.get(destination.getName());
        if(method != null) {
            try {
                result = method.invoke(rs, new Object[]{new Integer(idx)});
                if(rs.wasNull()) return null;
                return result;
            } catch(IllegalAccessException e) {
                System.out.println("Unable to read from ResultSet: "+e);
            } catch(InvocationTargetException e) {
                System.out.println("Unable to read from ResultSet: "+e);
            }
        }

        result = rs.getObject(idx);
        if(result == null)
            return null;

        if(destination.isAssignableFrom(result.getClass()))
            return result;
// DEBUG        else System.out.println("Got a "+result.getClass().getName()+": '"+result+"' while looking for a "+destination.getName());

        // Also we should detect the EJB references here

        // Get the underlying byte[]

        byte[] bytes = result instanceof byte[] ? (byte[])result : rs.getBytes(idx);
		//byte[] bytes = rs.getBytes(idx);

        if( bytes == null ) {
            result = null;
        } else {
           // We should really reuse these guys

            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

           // Use the class loader to deserialize

            try {
                WorkaroundInputStream ois = new WorkaroundInputStream(bais);
                result = ois.readObject();
				
				// ejb-reference: get the object back from the handle
				if (result instanceof Handle) result = ((Handle)result).getEJBObject();
			
                if(!destination.isAssignableFrom(result.getClass())) {
                    System.out.println("Unable to load a ResultSet column into a variable of type '"+destination.getName()+"' (got a "+result.getClass().getName()+")");
                    result = null;
                }

                ois.close();
			} catch (RemoteException e) {
				throw new SQLException("Unable to load EJBObject back from Handle: " +e);
            } catch (IOException e) {
                throw new SQLException("Unable to load a ResultSet column into a variable of type '"+destination.getName()+"': "+e);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Unable to load a ResultSet column into a variable of type '"+destination.getName()+"': "+e);
            }
        }

        return result;
    }

   /**
    * Gets the integer JDBC type code corresponding to the given name.
    *
    * @param name the JDBC type name.
    * @return the JDBC type code.
    * @see Types
    */
   protected final int getJDBCType(String name)
   {
      try
      {
         Integer constant = (Integer)Types.class.getField(name).get(null);
         return constant.intValue();
      } catch (Exception e)
      {
         // JF: Dubious - better to throw a meaningful exception
         Logger.exception(e);
         return Types.OTHER;
      }
   }

   /**
    * Gets the JDBC type name corresponding to the given type code.
    *
    * @param jdbcType the integer JDBC type code.
    * @return the JDBC type name.
    * @see Types
    */
   protected final String getJDBCTypeName(int jdbcType)
   {
      if (jdbcTypeNames == null)
      {
         setUpJDBCTypeNames();
      }

      return (String)jdbcTypeNames.get(new Integer(jdbcType));
   }

   /**
    * Returns the comma-delimited list of primary key column names
    * for this entity.
    *
    * @return comma-delimited list of primary key column names.
    */
   protected final String getPkColumnList()
   {
      StringBuffer sb = new StringBuffer();
      Iterator it = jawsEntity.getPkFields();
      while (it.hasNext())
      {
         PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
         sb.append(pkFieldMetaData.getColumnName());
         if (it.hasNext())
         {
            sb.append(",");
         }
      }
      return sb.toString();
   }

   /**
    * Returns the string to go in a WHERE clause based on
    * the entity's primary key.
    *
    * @return WHERE clause content, in the form
    *  <code>pkCol1Name=? AND pkCol2Name=?</code>
    */
   protected final String getPkColumnWhereList()
   {
      StringBuffer sb = new StringBuffer();
      Iterator it = jawsEntity.getPkFields();
      while (it.hasNext())
      {
         PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
         sb.append(pkFieldMetaData.getColumnName());
         sb.append("=?");
         if (it.hasNext())
         {
            sb.append(" AND ");
         }
      }
      return sb.toString();
   }

   // MF: PERF!!!!!!!
   protected Object[] getState(EntityEnterpriseContext ctx)
   {
      Object[] state = new Object[jawsEntity.getNumberOfCMPFields()];
      Iterator iter = jawsEntity.getCMPFields();
      int i = 0;
      while (iter.hasNext())
      {
         CMPFieldMetaData fieldMetaData = (CMPFieldMetaData)iter.next();
         try
         {
            // JF: Should clone
            state[i++] = getCMPFieldValue(ctx.getInstance(), fieldMetaData);
         } catch (Exception e)
         {
            return null;
         }
      }

      return state;
   }
   
   protected Object getCMPFieldValue(Object instance, CMPFieldMetaData fieldMetaData)
      throws IllegalAccessException
   {
      Field field = fieldMetaData.getField();
      return field.get(instance);
   }

   protected void setCMPFieldValue(Object instance,
                                   CMPFieldMetaData fieldMetaData,
                                   Object value)
      throws IllegalAccessException
   {
      Field field = fieldMetaData.getField();
      field.set(instance, value);
   }
   
   protected Object getPkFieldValue(Object pk, PkFieldMetaData pkFieldMetaData)
      throws IllegalAccessException
   {
      Field field = pkFieldMetaData.getPkField();
      return field.get(pk);
   }

   // This is now only used in setForeignKey
   protected int getJawsCMPFieldJDBCType(CMPFieldMetaData fieldMetaData)
   {
      return fieldMetaData.getJDBCType();
   }

   // Private -------------------------------------------------------

   /** Get a database connection */
   protected Connection getConnection() throws SQLException
   {
      DataSource ds = jawsEntity.getDataSource();
      if (ds != null)
      {
         return ds.getConnection();
      } else throw new RuntimeException("Unable to locate data source!");
   }

   private final void setUpJDBCTypeNames()
   {
      jdbcTypeNames = new HashMap();

      Field[] fields = Types.class.getFields();
      int length = fields.length;
      for (int i = 0; i < length; i++) {
         Field f = fields[i];
         String fieldName = f.getName();
         try {
            Object fieldValue = f.get(null);
            jdbcTypeNames.put(fieldValue, fieldName);
         } catch (IllegalAccessException e) {
            // Should never happen
            Logger.exception(e);
         }
      }
   }

    class WorkaroundInputStream extends ObjectInputStream {
        public WorkaroundInputStream(java.io.InputStream source) throws IOException, java.io.StreamCorruptedException{
            super(source);
        }
        protected Class resolveClass(java.io.ObjectStreamClass v) throws IOException, ClassNotFoundException {
            try {
                return Class.forName(v.getName(), false, Thread.currentThread().getContextClassLoader());
            } catch(Exception e) {}
            return super.resolveClass(v);
        }
    }
}
