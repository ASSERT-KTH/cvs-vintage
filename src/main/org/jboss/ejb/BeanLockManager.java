/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
*/

package org.jboss.ejb;

import java.util.HashMap;

import org.jboss.ejb.Container;

/**
 * Manages BeanLocks.  All BeanLocks have a reference count.
 * When the reference count goes to 0, the lock is released from the
 * id -> lock mapping.
 *
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 *
 * @version $Revision: 1.6 $
 * <p><b>Revisions:</b><br>
 * <p><b>20010802: marcf</b>
 * <ol>
 *   <li>Made bean lock pluggable, container factory passes in lockClass
 *   <li>Removed un-used constructor, added getters and setters
 * </ol>
 */
public class BeanLockManager
{
   private HashMap map = new HashMap();

   /** The container this manager reports to */
   private Container container;

   /** Reentrancy of calls */
   private boolean reentrant = false;
   private int txTimeout = 5000;
	
   public Class lockClass;

   /**
    * returns the lock associated with the key passed.  If there is
    * no lock one is created this call also increments the number of 
    * references interested in Lock.
    * 
    * WARNING: All access to this method MUST have an equivalent 
    * removeLockRef cleanup call, or this will create a leak in the map,  
    */
   public synchronized BeanLock getLock(Object id)
   {
      BeanLock lock = (BeanLock)map.get(id);
      if (lock == null)
      {
         try {
            lock = (BeanLock) lockClass.newInstance();
            
            lock.setId(id);
            lock.setReentrant(reentrant);
            lock.setTimeout(txTimeout);
         }
         catch (Exception e ) {e.printStackTrace();}
         
         map.put(id, lock);
      }
      lock.addRef();
	
      return lock;
   }

   public synchronized void removeLockRef(Object id)
   {
      BeanLock lock = (BeanLock)map.get(id);
      if (lock != null)
      {
         lock.removeRef();
         if (lock.getRefs() <= 0)
         {
            map.remove(lock.getId());
         }
      }
   }
	
   public void setLockCLass(Class lockClass) {this.lockClass=lockClass;}
   public void setReentrant(boolean reentrant) {this.reentrant = reentrant;}
}

