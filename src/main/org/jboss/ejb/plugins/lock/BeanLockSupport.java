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
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;


/**
 * Support for the BeanLock
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.9 $
 *
 * <p><b>Revisions:</b><br>
 *  <p><b>2001/07/29: marcf</b>
 *  <ol>
 *   <li>Initial revision
 * </ol>
 */
public abstract class BeanLockSupport
   implements BeanLock
{
   /**
    * Number of threads invoking methods on this bean
    * (1 normally >1 if reentrant)
    */
   protected int numMethodLocks = 0;
   
   /**
    * Number of threads that retrieved this lock from the manager
    * (0 means removing)
    */ 
   protected int refs = 0;
   
   /** The Cachekey corresponding to this Bean */
   protected Object id = null;
 
   /** Are reentrant calls allowed? */
   protected boolean reentrant;
 
   /** Use a JBoss custom log4j category for trace level logging */
   static Logger log = Logger.getLogger(BeanLock.class);
 
   protected Transaction tx = null;
 
   protected boolean synched = false;

   protected int txTimeout;

	
   public void setId(Object id) { this.id = id;}
   public Object getId() { return id;}
   public void setReentrant(boolean reentrant) {this.reentrant = reentrant;}
   public void setTimeout(int timeout) {txTimeout = timeout;}
	
   public void sync()
   {
      synchronized(this)
      {
         while(synched)
         {
            try
            {
               this.wait();
            }
            catch (InterruptedException ex) { /* ignore */ }
         }
         synched = true;
      }
   }
 
   public void releaseSync()
   {
      synchronized(this)
      {
         synched = false;
         this.notify();
      }
   }
 
   public abstract void schedule(Invocation mi) throws Exception;
	
   /**
    * The setTransaction associates a transaction with the lock.
    * The current transaction is associated by the schedule call.
    */
   public void setTransaction(Transaction tx){this.tx = tx;}
   public Transaction getTransaction(){return tx;}
   
   public abstract void endTransaction(Transaction tx);
   public abstract void wontSynchronize(Transaction tx);
	
   public boolean isMethodLocked() { return numMethodLocks > 0;}
   public int getNumMethodLocks() { return numMethodLocks;}
   public void addMethodLock() { numMethodLocks++; }
	
   public abstract void releaseMethodLock();
   
   public void addRef() { refs++;}
   public void removeRef() { refs--;}
   public int getRefs() { return refs;}
   
   // Private --------------------------------------------------------
   
   private static final Method getEJBHome;
   private static final Method getHandle;
   private static final Method getPrimaryKey;
   private static final Method isIdentical;
   private static final Method remove;
   
   static
   {
      try
      {
         Class[] noArg = new Class[0];
         getEJBHome = EJBObject.class.getMethod("getEJBHome", noArg);
         getHandle = EJBObject.class.getMethod("getHandle", noArg);
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", noArg);
         isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] {EJBObject.class});
         remove = EJBObject.class.getMethod("remove", noArg);
      }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   protected boolean isCallAllowed(Invocation mi)
   {
      // is this a reentrant bean
      if (reentrant)
      {
         return true;
      }

      // is this a known non-entrant method
      Method m = mi.getMethod();
      if (m.equals(getEJBHome) ||
          m.equals(getHandle) ||
          m.equals(getPrimaryKey) ||
          m.equals(isIdentical) ||
          m.equals(remove))
      {
         return true;
      }

      // if this is a non-entrant message to the container let it through
      if(NonentrantMessage.class.isAssignableFrom( m.getDeclaringClass() )) 
      {
         return true;
      }
  
      return false;
   }
}
