/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.math.BigDecimal;

import java.lang.reflect.Field;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import javax.sql.DataSource;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.CMPFieldInfo;
import org.jboss.ejb.plugins.jaws.MetaInfo;
import org.jboss.ejb.plugins.jaws.PkFieldInfo;
import org.jboss.ejb.plugins.jaws.deployment.JawsEntity;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;
import org.jboss.logging.Log;
import org.jboss.logging.Logger;

/**
 * Abstract superclass for all JAWS Commands that use JDBC directly.
 * Provides a Template Method for jdbcExecute(), default implementations
 * for some of the methods called by this template, and a bunch of
 * utility methods that database commands may need to call.
 *
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.6 $
 */
public abstract class JDBCCommand
{
   // Attributes ----------------------------------------------------
   
   protected JDBCCommandFactory factory;
   protected MetaInfo metaInfo;
   protected Log log;
   protected String name;    // Command name, used for debug trace
   
   private String sql;
   private static Map jdbcTypeNames;
   
   /**
    * Gives compile-time control of tracing.
    */
   public static boolean debug = true;
   
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
      this.metaInfo = factory.getMetaInfo();
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
      
      if (value == null)
      {
         stmt.setNull(idx, jdbcType);
      } else
      {
         stmt.setObject(idx, value, jdbcType);
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
      Iterator it = metaInfo.getPkFieldInfos();
      
      if (metaInfo.hasCompositeKey())
      {
         while (it.hasNext())
         {
            PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
            int jdbcType = pkFieldInfo.getJDBCType();
            Object value = getPkFieldValue(id, pkFieldInfo);
            setParameter(stmt, parameterIndex++, jdbcType, value);
         }
      } else
      {
         PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
         int jdbcType = pkFieldInfo.getJDBCType();
         setParameter(stmt, parameterIndex++, jdbcType, id);
      }
      
      return parameterIndex;
   }
   
   /**
    * Sets parameter(s) representing a foreign key in this 
    * Command's PreparedStatement.
    * TODO: (JF) tighten up the typing of the value parameter.
    *
    * @param stmt the PreparedStatement whose parameters need to be set.
    * @param idx the index (1-based) of the first parameter to be set.
    * @param fieldInfo the CMP meta-info for the field containing the 
    *  entity reference.
    * @param value the entity (EJBObject) referred to by the reference
    *  (may be null).
    * @return the index of the next unset parameter.
    * @throws SQLException if the access to the referred-to entity's primary
    *  key fails, or if parameter setting fails.
    */
   protected int setForeignKey(PreparedStatement stmt,
                             int idx,
                             CMPFieldInfo fieldInfo,
                             Object value)
      throws SQLException
   {
      JawsCMPField[] pkInfo = fieldInfo.getForeignKeyCMPFields();
      Object pk = null;
      
      if (value != null)
      {
         try
         {
            pk = ((EJBObject)value).getPrimaryKey();
         } catch (RemoteException e)
         {
            throw new SQLException("Could not extract primary key from EJB reference:"+e);
         }
      }
      
      if (!((JawsEntity)pkInfo[0].getBeanContext()).getPrimaryKeyField().equals(""))
      {
         // Primitive key
         int jdbcType = getJawsCMPFieldJDBCType(pkInfo[0]);
         Object fieldValue = (value == null) ? null : pk;
         setParameter(stmt, idx, jdbcType, fieldValue);
         return idx+1;
      } else
      {
         // Compound key
         Field[] fields = (value == null) ? null : pk.getClass().getFields();
         try
         {
            for (int i = 0; i < pkInfo.length; i++)
            {
               int jdbcType = getJawsCMPFieldJDBCType(pkInfo[i]);
               Object fieldValue = (value == null) ? null : fields[i].get(pk);
               setParameter(stmt, idx+i, jdbcType, fieldValue);
            }
         } catch (IllegalAccessException e)
         {
            throw new SQLException("Could not extract fields from primary key:"+e);
         }
         return idx+pkInfo.length;
      }
   }
   
   /**
    * Used for all retrieval of results from <code>ResultSet</code>s.
    * Implements tracing, and allows some tweaking of returned types.
    *
    * @param rs the <code>ResultSet</code> from which a result is being retrieved.
    * @param idx index of the result column.
    * @param jdbcType the JDBC type which this result is expected to be 
    *        compatible with.
    */
   protected Object getResultObject(ResultSet rs, int idx, int jdbcType)
      throws SQLException
   {
      Object result = rs.getObject(idx);
      
      // Result transformation required by Oracle, courtesy of Jay Walters
      
      if (result instanceof BigDecimal)
      {
         BigDecimal bigDecResult = (BigDecimal)result;
         
         switch (jdbcType)
         {
            case Types.INTEGER:
               result = new Integer(bigDecResult.intValue());
               break;
            
            case Types.BIT:
               result = new Boolean(bigDecResult.intValue() > 0);
               break;
            
            case Types.DOUBLE:
               result = new Double(bigDecResult.doubleValue());
               break;
            
            case Types.FLOAT:
               result = new Float(bigDecResult.floatValue());
               break;
            
            case Types.BIGINT:
               result = new Long(bigDecResult.longValue());
               break;
         }
      }
      
      if (debug) {
         String className = result == null ? "null" : result.getClass().getName();
         log.debug("Got result: idx=" + idx +
                   ", value=" + result +
                   ", class=" + className +
                   ", JDBCtype=" + getJDBCTypeName(jdbcType));
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
      Iterator it = metaInfo.getPkFieldInfos();
      while (it.hasNext())
      {
         PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
         sb.append(pkFieldInfo.getColumnName());
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
      Iterator it = metaInfo.getPkFieldInfos();
      while (it.hasNext())
      {
         PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
         sb.append(pkFieldInfo.getColumnName());
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
      Object[] state = new Object[metaInfo.getNumberOfCMPFields()];
      Iterator iter = metaInfo.getCMPFieldInfos();
      int i = 0;
      while (iter.hasNext())
      {
         CMPFieldInfo fieldInfo = (CMPFieldInfo)iter.next();
         try
         {
            // JF: Should clone
            state[i++] = getCMPFieldValue(ctx.getInstance(), fieldInfo);
         } catch (Exception e)
         {
            return null;
         }
      }
   
      return state;
   }
   
   protected Object getCMPFieldValue(Object instance, CMPFieldInfo fieldInfo)
      throws IllegalAccessException
   {
      Field field = fieldInfo.getField();
      return field.get(instance);
   }
   
   protected void setCMPFieldValue(Object instance,
                                   CMPFieldInfo fieldInfo,
                                   Object value)
      throws IllegalAccessException
   {
      Field field = fieldInfo.getField();
      field.set(instance, value);
   }
   
   protected Object getPkFieldValue(Object pk, PkFieldInfo pkFieldInfo)
      throws IllegalAccessException
   {
      Field field = pkFieldInfo.getPkField();
      return field.get(pk);
   }
   
   // This is now only used in setForeignKey
   
   protected int getJawsCMPFieldJDBCType(JawsCMPField fieldInfo)
   {
      return getJDBCType(fieldInfo.getJdbcType());
   }
   
   // Private -------------------------------------------------------
   
   /** Get a database connection */
   private Connection getConnection() throws SQLException
   {
      DataSource ds = metaInfo.getDataSource();
      if (ds != null)
      {
         return ds.getConnection();
      } else
      {
         String url = metaInfo.getDbURL();
         return DriverManager.getConnection(url,"sa","");
      }
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
}
