/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.lock;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.HashMap;

import javax.transaction.Transaction;
import javax.transaction.Status;

import org.jboss.invocation.Invocation;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.w3c.dom.Element;
import org.jboss.metadata.MetaData;
import javax.naming.InitialContext;
/**
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 *
 * @version $Revision: 1.2 $
 */
public class JDBCRowLock extends BeanLockSupport
{
   protected DataSource ds = null;
   protected String preparedStatementString = null;

   public void setConfiguration(Element config)
   {
      try
      {
         this.config = config;
         String dataSourceName = MetaData.getOptionalChildContent(config, "datasource");
         ds = (DataSource)new InitialContext ().lookup (dataSourceName);
         String prep = MetaData.getOptionalChildContent(config, "jdbc-statement");
         int mark = 0;
         while ( (mark = prep.indexOf('?', mark)) != -1)
         {
            String before = prep.substring(0, mark);
            String after = prep.substring(mark + 1);
            prep = before + id.toString() + after;
         }
         log.info("JDBCRowLock ps: " + prep);
         preparedStatementString = prep;
      } 
      catch (Exception ex)
      {
         throw new RuntimeException("Failed to initialized Bean Lock: " + id.toString());
      }
   }

   public void schedule(Invocation mi) throws Exception
   {
      boolean threadScheduled = false;
      while (!threadScheduled)
      {
         /* loop on lock wakeup and restart trying to schedule */
         threadScheduled = doSchedule(mi);
      }
   }

   protected boolean isTxExpired(Transaction miTx) throws Exception
   {
      if (miTx != null && miTx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
      {
         return true;
      }
      return false;
   }

   /**
    * doSchedule(Invocation)
    * 
    * doSchedule implements a particular policy for scheduling the threads coming in. 
    * 
    */
   protected boolean doSchedule(Invocation mi) 
      throws Exception
   {
      Transaction miTx = mi.getTransaction();
      boolean trace = log.isTraceEnabled();
  
      if (isTxExpired(miTx))
      {
         log.error("Saw rolled back tx="+miTx);
         throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
      }
      
      if (miTx != null
          && !container.getBeanMetaData().isMethodReadOnly(mi.getMethod().getName()))
      {
         //Next test is independent of whether the context is locked or not, it is purely transactional
         // Is the instance involved with another transaction? if so we implement pessimistic locking
         waitForTx(trace);
      }

      //If we reach here we are properly scheduled to go through so return true
      return true;
   } 

   /**
    * Wait until no other transaction is running with this lock.
    *
    */
   protected void waitForTx(boolean trace) throws Exception
   {
      Connection conn = null;
      PreparedStatement ps = null;
      try
      {
         conn = ds.getConnection();
         ps = conn.prepareStatement(preparedStatementString);
         ResultSet rs = ps.executeQuery();
         rs.close();
         // Connection commit/rollback should release DB row lock
      }
      finally
      {
         if (ps != null) { 
            try { ps.close(); } catch (Exception ignored) {} 
         }
         if (conn != null) { 
            try { conn.close(); } catch (Exception ignored) {} 
         }
      }
      
   }

   /*
    * nextTransaction()
    *
    */
   protected void nextTransaction() 
   {
      // nothing needed
      // Connection commit/rollback should release DB row lock
   }
   
   public void endTransaction(Transaction transaction)
   {
      nextTransaction();
   }

   public void wontSynchronize(Transaction trasaction)
   {
      nextTransaction();
   }
   
   /**
    * releaseMethodLock
    *
    * if we reach the count of zero it means the instance is free from threads (and reentrency)
    * we wake up the next thread in the currentLock
    */
   public void endInvocation(Invocation mi)
   { 
   }
   
}

