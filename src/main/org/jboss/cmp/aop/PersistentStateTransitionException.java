/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cmp.aop;

/**
 * The exception is thrown in attempt of illegal state transition.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public class PersistentStateTransitionException
   extends PersistenceException
{
   // Constructor ------------------------------------------
   public PersistentStateTransitionException()
   {
      super();
   }

   public PersistentStateTransitionException(String s)
   {
      super(s);
   }
}
