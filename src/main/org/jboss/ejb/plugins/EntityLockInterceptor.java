/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.RemoveException;
import javax.ejb.EntityBean;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.BeanLock;
import org.jboss.ejb.BeanLockManager;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.CacheKey;
import org.jboss.logging.log4j.JBossCategory;
import org.jboss.metadata.EntityMetaData;
import org.jboss.tm.TxManager;

/**
 * The lock interceptors role is to schedule thread wanting to invoke method on a target bean
 *
* <p>The policies for implementing scheduling (pessimistic locking etc) is implemented by pluggable
*    locks
*
* <p>We also implement serialization of calls in here (this is a spec
*    requirement). This is a fine grained notify, notifyAll mechanism. We
*    notify on ctx serialization locks and notifyAll on global transactional
*    locks.
*   
* <p><b>WARNING: critical code</b>, get approval from senior developers
*    before changing.
*    
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
* @version $Revision: 1.4 $
*
* <p><b>Revisions:</b><br>
* <p><b>2001/07/30: marcf</b>
* <ol>
*   <li>Initial revision
*   <li>Factorization of the lock out of the context in cache
*   <li>The new locking is implement as "scheduling" in the lock which allows for pluggable locks
* </ol>
*/
public class EntityLockInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
 
   // Attributes ----------------------------------------------------
 
   protected EntityContainer container;
 
   // Static --------------------------------------------------------
 
   /** Use a JBoss custom log4j category for trace level logging */
   static JBossCategory log = (JBossCategory) JBossCategory.getInstance(EntityLockInterceptor.class);
 
   // Constructors --------------------------------------------------
 
   // Public --------------------------------------------------------
 
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
 
   }
 
   public Container getContainer()
   {
      return container;
   }
 
   // Interceptor implementation --------------------------------------
 
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {  
      // Invoke through interceptors
      return getNext().invokeHome(mi);
  
   }
 
   public Object invoke(MethodInvocation mi)
      throws Exception
   {
  
      // The key.
      Object key = (CacheKey) mi.getId();
  
      // The lock.
      BeanLock lock ;
  
      boolean threadIsScheduled = false;
      boolean trace = log.isTraceEnabled();
  
      if( trace ) log.trace("Begin invoke, key="+key);
   
  
      try 
      {
         lock = (BeanLock)container.getLockManager().getLock(key);
   
         while (!threadIsScheduled)
         { 
            if( trace ) log.trace("new while(threadNotScheduled) for key"+key);
     
    
            /**
             * schedule will implement the actual lock policy in the lock
             * we provide a default "pessimistic lock" on the transaction
             * as well as the default serialization required by the specification
             * the method releases the sync if the thread isn't scheduled and goes to wait
             * upon waking up it must go through the schedule again
             * Also schedule must add a methodLock
             */
            threadIsScheduled = lock.schedule(mi);
         }
   
         try {
    
            return getNext().invoke(mi); 
         }
   
         finally 
         {
    
            // we are done with the method, decrease the count, if it reaches 0 it will wake up 
            // the next thread 
            lock.sync();
            lock.releaseMethodLock();
            lock.releaseSync(); 
         }
      }
      finally
      {
   
         // We are done with the lock in general
         container.getLockManager().removeLockRef(key);
   
         if( trace ) log.trace("End invoke, key="+key);
      }
   }
}




