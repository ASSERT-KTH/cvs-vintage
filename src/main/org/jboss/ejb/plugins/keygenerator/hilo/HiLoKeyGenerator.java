/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.keygenerator.hilo;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.4 $</tt>
 */
public class HiLoKeyGenerator
   implements KeyGenerator
{
   private static long highestHi = 0;

   private final Logger log;
   private final DataSource ds;
   private final long blockSize;

   private long hi;
   private long lo;

   private TransactionManager tm;
   private String updateHiSql;

   public HiLoKeyGenerator(
      DataSource ds,
      String tableName,
      String sequenceColumn,
      String sequenceName,
      String idColumnName,
      long blockSize,
      TransactionManager tm
      )
   {
      this.ds = ds;
      this.blockSize = blockSize;
      this.tm = tm;
      this.log = Logger.getLogger(getClass().getName() + "#" + tableName + "_" + sequenceName);

      updateHiSql = "update " +
         tableName +
         " set " +
         idColumnName +
         "=?" +
         " where " + sequenceColumn + "='" + sequenceName + "'";
   }

   public synchronized Object generateKey()
   {
      if(lo < hi)
      {
         ++lo;
      }
      else
      {
         Transaction curTx = null;
         try
         {
            curTx = tm.suspend();
         }
         catch(SystemException e)
         {
            throw new IllegalStateException("Failed to suspend current transaction.");
         }

         try
         {
            tm.begin();
         }
         catch(Exception e)
         {
            throw new IllegalStateException("Failed to begin a new transaction.");
         }

         lo = highestHi + 1;
         highestHi += blockSize;
         hi = highestHi;

         try
         {
            updateTable();
            tm.commit();
         }
         catch(SQLException e)
         {
            log.error("Failed to update table: " + e.getMessage(), e);

            try
            {
               tm.rollback();
            }
            catch(SystemException e1)
            {
               log.error("Failed to rollback.", e1);
            }

            throw new IllegalStateException(e.getMessage());
         }
         catch(Exception e)
         {
            log.error("Failed to commit.", e);
         }
         finally
         {
            if(curTx != null)
            {
               try
               {
                  tm.resume(curTx);
               }
               catch(Exception e)
               {
                  throw new IllegalStateException("Failed to resume transaction: " + e.getMessage());
               }
            }
         }
      }

      return new Long(lo);
   }

   private void updateTable() throws SQLException
   {
      Connection con = null;
      PreparedStatement updateHi = null;

      if(log.isTraceEnabled())
      {
         log.trace("Executing SQL: " + updateHiSql + ", [" + highestHi + "]");
      }

      try
      {
         con = ds.getConnection();
         updateHi = con.prepareStatement(updateHiSql);
         updateHi.setLong(1, highestHi);
         final int i = updateHi.executeUpdate();

         if(i != 1)
         {
            throw new SQLException("Expected one updated row but got: " + i);
         }
      }
      finally
      {
         JDBCUtil.safeClose(updateHi);
         JDBCUtil.safeClose(con);
      }
   }
}