/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cmp.aop;

/**
 * This interface is implemented by classes that are able to respond
 * to state interrogations.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public interface StateAware
{
   /**
    * Indicates whether the state of the instance was changed in the current transaction.
    *
    * @return true if the state of the instance was changed in the current transaction;
    *         false if the state wasn't changed or the instance is transient.
    */
   boolean cmpIsDirty();

   /**
    * Indicates whether the instance represents data in the physical store.
    *
    * @return true if the instance represents data in the physical store;
    *         false otherwise.
    */
   boolean cmpIsPersistent();

   /**
    * Indicates whether the instance was made persistent in the current transaction.
    *
    * @return true if the instance was made persistent in the current transaction;
    *         false otherwise.
    */
   boolean cmpIsNew();

   /**
    * Indicates whether the instance was marked as deleted in the current transaction.
    *
    * @return true if the instance was marked as deleted in the current transaction;
    *         false otherwise.
    */
   boolean cmpIsDeleted();
}
