/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cmp.aop;

/**
 * This interface is implemented by PersistentStateManager.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public interface PersistentStateEventListener
{
   /**
    * The method represents the callback on the listener when
    * state event occured on an instance.
    *
    * @param event The event occured.
    * @exception PersistentStateTransitionException Throws exception if the event
    * is not allowed for the state the instance is in.
    */
   void processPersistentStateEvent(PersistentStateTransitionEvent event)
      throws PersistentStateTransitionException;
}
