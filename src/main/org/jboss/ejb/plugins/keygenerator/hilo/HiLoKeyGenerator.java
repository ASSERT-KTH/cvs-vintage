/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.keygenerator.hilo;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import javax.transaction.InvalidTransactionException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

/**
 * The implementation of HiLo key generator
 *
 * @@jmx.mbean
 *    name="jboss:service=HiLoKeyGeneratorFactory"
 *    extends="org.jboss.ejb.plugins.keygenerator.hilo.HiLoGeneratorMBean"
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 *
 * @version $Revision: 1.1 $
 */
public class HiLoKeyGenerator
   implements KeyGenerator
{
   // Attributes ---------------------------------------------------
   static Logger log = Logger.getLogger(HiLoKeyGenerator.class);
   static private boolean isDebugEnabled = log.isDebugEnabled();
   private int hi;
   private int lo;
   private final int blockSize;
   private final String tableName;
   private final String columnName;
   private final String columnSQLType;
   private final String nextKeyQuery;
   private final String updateStmt;
   private final DataSource datasource;
   private final TransactionManager tm;

   // Constructor --------------------------------------------------
   public HiLoKeyGenerator(String datasourceName,
                           String tmName,
                           String table,
                           String columnName,
                           String columnSqlType,
                           int blockSize)
      throws Exception
   {
      this.tableName = table;
      this.columnName = columnName;
      this.columnSQLType = columnSqlType;
      this.blockSize = blockSize;

      this.nextKeyQuery = "SELECT " + columnName + " FROM " + table;
      if(isDebugEnabled) log.debug("nextKeyQuery: " + nextKeyQuery);
      this.updateStmt = "UPDATE " + table + " SET " + columnName + "=? WHERE " + columnName + "=?";
      if(isDebugEnabled) log.debug("updateStmt: " + updateStmt);

      try
      {
         Context ctx = new InitialContext();
         datasource = (DataSource)ctx.lookup(datasourceName);
         tm = (TransactionManager)ctx.lookup(tmName);
      }
      catch(NamingException e)
      {
         throw e;
      }

      if(!tableExists())
         initTable();
   }

   // KeyGenerator implementation ----------------------------------
   public synchronized Object generateKey()
   {
      if(lo < hi)
        return new Integer(lo++);

      Connection con = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      Transaction currentTx = null;
      try
      {
         currentTx = tm.suspend();
         tm.begin();

         con = datasource.getConnection();
         int rowsUpdated;
         do
         {
            if(isDebugEnabled) log.debug("Executing SQL: " + nextKeyQuery);
            stmt = con.prepareStatement(nextKeyQuery);
            rs = stmt.executeQuery();
            if(!rs.next())
               throw new Exception("Couldn't read next key: ResultSet is empty.");

            lo = rs.getInt(1);
            hi = lo + blockSize;

            if(isDebugEnabled)
               log.debug("Executing SQL: " + updateStmt + " [p1=" + hi + ",p2=" + lo + "]");
            stmt = con.prepareStatement(updateStmt);
            stmt.setInt(1, hi);
            stmt.setInt(2, lo);
            rowsUpdated = stmt.executeUpdate();
         }
         while(rowsUpdated == 0);

         tm.commit();
      }
      catch(Exception e)
      {
         log.error("Error fetching next key:", e);
         try
         {
            tm.rollback();
         }
         catch(SystemException se)
         {
            log.error("Couldn't rollback transaction: ", se);
         }
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(stmt);
         JDBCUtil.safeClose(con);

         if(currentTx != null)
         {
            try
            {
               tm.resume(currentTx);
            }
            catch(InvalidTransactionException ite)
            {
               log.error("Invalid transaction: ", ite);
            }
            catch(SystemException se)
            {
               log.error("Unexpected error: ", se);
            }
         }
      }
      return new Integer(lo++);
   }

   // Private -----------------------------------------------------
   /**
    * @throws SQLException
    */
   public void dropTable()
      throws SQLException
   {
      Connection con = null;
      PreparedStatement stmt = null;
      try
      {
         String sql = "DROP TABLE " + tableName;
         if(isDebugEnabled) log.debug("Executing SQL: " + sql);
         con = datasource.getConnection();
         stmt = con.prepareStatement(sql);
         stmt.execute();
      }
      catch(SQLException sqle)
      {
         log.error("Couldn't drop tableName '" + tableName + "'");
         throw sqle;
      }
      finally
      {
         JDBCUtil.safeClose(con);
         JDBCUtil.safeClose(stmt);
      }
   }

   /**
    * @throws SQLException
    */
   public void initTable()
      throws SQLException
   {
      Connection con = null;
      PreparedStatement stmt = null;
      try
      {
         String sql = getCreateTableSQL();
         if(isDebugEnabled) log.debug("Executing SQL: " + sql);

         con = datasource.getConnection();
         stmt = con.prepareStatement(sql);
         stmt.execute();

         sql = "INSERT INTO " + tableName + " VALUES(1)";
         if(isDebugEnabled) log.debug("Executing SQL: " + sql);
         stmt = con.prepareStatement(sql);
         int rows = stmt.executeUpdate();
         if(rows != 1)
            log.error("Error while initializing tableName! rows inserted: " + rows);
      }
      catch(SQLException sqle)
      {
         log.error("Couldn't create tableName '" + tableName + "'");
         throw sqle;
      }
      finally
      {
         JDBCUtil.safeClose(con);
         JDBCUtil.safeClose(stmt);
      }
   }

   private String getCreateTableSQL()
   {
      StringBuffer sql = new StringBuffer(100);
      sql.append("CREATE TABLE ").
         append(tableName).
         append(" (").append(columnName).append(" ").append(columnSQLType).
         append(" NOT NULL)");
      return sql.toString();
   }

   private boolean tableExists()
      throws SQLException
   {
      Connection con = null;
      ResultSet rs = null;
      try
      {
         con = datasource.getConnection();
         DatabaseMetaData dmd = con.getMetaData();
         rs = dmd.getTables(con.getCatalog(), null, tableName, null);
         return rs.next();
      }
      catch(SQLException e)
      {
         // This should not happen. A J2EE compatiable JDBC driver is
         // required fully support metadata.
         throw e;
      }
      finally
      {
         JDBCUtil.safeClose(rs);
         JDBCUtil.safeClose(con);
      }
   }
}
