/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.plugins.lock.QueuedPessimisticEJBLock;

/**
 * A example of testing the method to see if is not supposed
 * to be entrant.
 */
public class NewQueuedPessimisticEJBLock
   extends QueuedPessimisticEJBLock
{
   protected boolean isCallAllowed(MethodInvocation mi)
   {
      return super.isCallAllowed(mi) ||
            NonentrantMessage.class.isAssignableFrom(
                  mi.getMethod().getDeclaringClass());
   }
}
