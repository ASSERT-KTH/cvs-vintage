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
 * @version $Revision: 1.1 $
 */
public class JDBCRowLock extends BeanLockSupport
{
   protected Object methodLock = new Object();
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

      this.sync();
      try
      {
         if( trace ) log.trace("Begin schedule, key="+mi.getId());
         // Here, we are trying to get the methodLock on the bean
         boolean acquiredMethodLock = false;
         while (!acquiredMethodLock)
         {
            acquiredMethodLock = attemptMethodLock(mi, trace);
            if (!acquiredMethodLock)
            {
               if (miTx != null)
               {
                  // This thread is involved with a transaction
                  // We need to check whether the transaction has timed
                  // out because we may have waited awhile in attemptMethodLock.
                  if (isTxExpired(miTx))
                  {
                     log.error("Saw rolled back tx="+miTx+" waiting for methodLock."
                               // +" On method: " + mi.getMethod().getName()
                               // +" txWaitQueue size: " + txWaitQueue.size()
                               );
                     throw new RuntimeException("Transaction marked for rollback, possibly a timeout");
                  }
               }
               else // non-transactional
               {
                  // we're non-transactional so we must return false
                  // and re-do lock acquisition logic
                  return false;
               }
            }
         } // end while(acquiredMethodLock)
         
         // We successfully acquired method lock!
      }
      finally
      {
         this.releaseSync();
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

   /**
    * Attempt to acquire a method lock.
    */
   protected boolean attemptMethodLock(Invocation mi, boolean trace) throws Exception
   {
      if (isMethodLocked()) 
      {
         // It is locked but re-entrant calls permitted (reentrant home ones are ok as well)
         if (!isCallAllowed(mi)) 
         {
            // This instance is in use and you are not permitted to reenter
            // Go to sleep and wait for the lock to be released
            // Threads finishing invocation will notify() on the lock
            if( trace ) log.trace("Thread contention on methodLock, Begin lock.wait(), id="+mi.getId());
            synchronized(methodLock)
            {
               releaseSync();
               try
               {
                  methodLock.wait(txTimeout);
               }
               catch (InterruptedException ignored) {}
            }
            this.sync();
            if( trace ) log.trace("End lock.wait(), id="+mi.getId()+", isLocked="+isMethodLocked());
            return false;
         }
         else
         { 
            //We are in a valid reentrant call so add a method lock
            addMethodLock();
         }
      }
      // No one is using that instance
      else 
      {
         // We are now using the instance
         addMethodLock();
      }
      // if we got here addMethodLock was called
      return true;
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
      numMethodLocks--;
      if (numMethodLocks == 0)
      {
         synchronized(methodLock) {methodLock.notify();}
      }
   }
   
}

