/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Field;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

/**
 * Abstract superclass for all JAWS Commands that use JDBC directly.
 * Provides a Template Method for jdbcExecute(), default implementations
 * for some of the methods called by this template, and a bunch of
 * utility methods that database commands may need to call.
 *
 * @see <related>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
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
   
   // Constructors --------------------------------------------------
   
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
    */
   protected void jdbcExecute() throws Exception
   {
      Connection con = null;
      PreparedStatement stmt = null;
      try
      {
         con = getConnection();
         String theSQL = getSQL();
         if (factory.debug)
         {
            log.debug(name + " command executing: " + theSQL);
         }
         stmt = con.prepareStatement(theSQL);
         setParameters(stmt);
         executeStatementAndHandleResult(stmt);
      } finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
         if (con != null)
         {
            try
            {
               con.close();
            } catch (SQLException e)
            {
               e.printStackTrace();
            }
         }
      }
   }
   
   /**
    * Used to set static SQL in subclass constructors.
    */
   protected void setSQL(String sql)
   {
      if (factory.debug)
      {
         log.debug(name + " SQL: " + sql);
      }
      this.sql = sql;
   }
   
   /**
    * Default implementation returns <code>sql</code> field value.
    * This is appropriate in all cases where static SQL can be
    * constructed in the Command constructor.
    * Override if dynamically-generated SQL, based on the arguments
    * given to execute(), is needed.
    */
   protected String getSQL() throws Exception
   {
      return sql;
   }
   
   /**
    * Default implementation does nothing.
    * Override if parameters need to be set.
    */
   protected void setParameters(PreparedStatement stmt) throws Exception
   {
   }
   
   /**
    * Execute the PreparedStatement and handle result of successful execution.
    * This is implemented in subclasses for queries and updates.
    */
   protected abstract void executeStatementAndHandleResult(
      PreparedStatement stmt) throws Exception;
   
   // ---------- Utility methods for use in subclasses ----------
   
   protected void setParameter(PreparedStatement stmt,
                               int idx,
                               int jdbcType,
                               Object value)
      throws SQLException
   {
      if (factory.debug)
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
   
   protected final int getJDBCType(String name)
   {
      try
      {
         Integer constant = (Integer)Types.class.getField(name).get(null);
         return constant.intValue();
      } catch (Exception e)
      {
         // JF: Dubious - better to throw a meaningful exception
         e.printStackTrace();
         return Types.OTHER;
      }
   }
   
   protected final String getJDBCTypeName(int jdbcType)
   {
      if (jdbcTypeNames == null)
      {
         setUpJDBCTypeNames();
      }
      
      return (String)jdbcTypeNames.get(new Integer(jdbcType));
   }
   
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
      
      // JF: Temp checks to narrow down bug
      if (pk == null) log.debug("***** getPkFieldValue: PK is null *****");
      if (field == null) log.debug("***** getPkFieldValue: Field is null *****");
      
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
            e.printStackTrace();
         }
      }
   }
}
