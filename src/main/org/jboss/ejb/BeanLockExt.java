/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb;

/** An extension of the BeanLock interface that adds support for non-blocking
 * acquisition of a lock.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public interface BeanLockExt extends BeanLock
{
   /**
    * A non-blocking method that checks if the calling thread will be able
    * to acquire the sync lock based on the calling thread.
    *
    * @return true if the calling thread can obtain the sync lock in which
    * case it will, false if another thread already has the lock.
    */
   public boolean attemptSync();
}
