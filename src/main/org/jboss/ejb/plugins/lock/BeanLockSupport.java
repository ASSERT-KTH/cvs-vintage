/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.lock;

import java.lang.reflect.Method;

import javax.transaction.Transaction;
import javax.ejb.EJBObject;

import org.jboss.ejb.BeanLock;
import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;

import org.w3c.dom.Element;

/**
 * Support for the BeanLock
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.27 $
 */
public abstract class BeanLockSupport
   implements BeanLock
{
   protected Container container = null;

   /** The Cachekey corresponding to this Bean */
   protected Object id = null;

   /** Logger instance */
   static Logger log = Logger.getLogger(BeanLock.class);

   /** Transaction holding lock on bean */
   protected Transaction tx = null;
   /** keep track of where lock was acquired.  This exception will be used as a stack trace */
   protected Exception traceOfLockHolder = null;

   protected Thread synched = null;
   protected int synchedDepth = 0;

   protected int txTimeout;

   protected Element config;

   public void setId(Object id) { this.id = id;}
   public Object getId() { return id;}
   public void setTimeout(int timeout) {txTimeout = timeout;}
   public void setContainer(Container container) { this.container = container; }
   public void setConfiguration(Element config) { this.config = config; }

   public void sync() throws InterruptedException
   {
      synchronized(this)
      {
         Thread thread = Thread.currentThread();
         while(synched != null && synched.equals(thread) == false)
         {
            this.wait();
         }
         synched = thread;
         ++synchedDepth;
      }
   }
   public void releaseSync()
   {
      synchronized(this)
      {
         if (--synchedDepth == 0)
            synched = null;
         this.notify();
      }
   }

   public boolean lockNoWait(Transaction transaction) throws Exception
   {
      return false;
   }

   public abstract void schedule(Invocation mi) throws Exception;

   /**
    * The setTransaction associates a transaction with the lock.
    * The current transaction is associated by the schedule call.
    */
   public void setTransaction(Transaction tx)
   {
      this.tx = tx;
   }
   public Transaction getTransaction(){return tx;}

   public abstract void endTransaction(Transaction tx);
   public abstract void wontSynchronize(Transaction tx);

   public abstract void endInvocation(Invocation mi);

   // This following is for deadlock detection
   protected static HashMap waiting = new HashMap();

   public void deadlockDetection(Transaction miTx)
      throws ApplicationDeadlockException
   {
      if (miTx == null)
         return;

      HashSet set = new HashSet();
      set.add(miTx);

      Object checkTx = this.tx;

      synchronized(waiting)
      {
          addWaiting(miTx);

          while (checkTx != null)
          {
             Object waitingFor = waiting.get(checkTx);
             if (waitingFor != null)
                waitingFor = ((BeanLock) waitingFor).getTransaction();
             if (waitingFor != null)
	       {
                if (set.contains(waitingFor))
                {
                   log.error("Application deadlock detected: " + miTx + " has deadlock conditions.  Two or more transactions contending for same resources and each have locks each other needs.");
                   if (log.isTraceEnabled())
                      log.trace("WAITING=" + waiting);
                   removeWaiting(miTx);
                   throw new ApplicationDeadlockException("Application deadlock detected: Two or more transactions contention.", true);
                 }
	           set.add(waitingFor);
             }
             checkTx = waitingFor;
          }
      }
   }

   /**
    * Add a transaction waiting for a lock
    */
   public void addWaiting(Transaction tx)
   {
      if (tx == null)
         throw new IllegalArgumentException("Attempt to addWaiting with a null transaction");
      synchronized (waiting)
      {
         waiting.put(tx, this);
      }
   }

   /**
    * Remove a transaction waiting for a lock
    */
   public void removeWaiting(Transaction tx)
   {
      if (tx == null)
         throw new IllegalArgumentException("Attempt to removeWaiting with a null transaction");
      synchronized (waiting)
      {
         waiting.remove(tx);
      }
   }
}
