/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.schema;

/**
 * Interface describing the attachment of an association to a class.
 */
public interface AbstractAssociationEnd
{
   /**
    * Return the name by which the class may access the association, or null
    * if the association cannot be navigated.
    * @return the name used to access the assocation, or null
    */
   public String getName();

   /**
    * Return the class returned by accessing this end, typically the
    * AbstractClass of the peer. For collections this is the type of
    * collection member and not of the collection implementation; for example,
    * if the end was a java.util.List containing String objects, the type would
    * be the AbstractType corresponding to String not to java.util.List
    * @return the class returned by accessing this end
    */
   public AbstractClass getType();

   /**
    * Indicate if the association can be navigated from this end
    * @return true if this end is navigable
    */
   public boolean isNavigable();

   /**
    * Indicate if this end returns a single value or a collection
    * @return true if the end returns a collection
    */
   public boolean isCollection();

   /**
    * Return the other end of the association
    * @return the other end of the association
    */
   public AbstractAssociationEnd getPeer();

   /**
    * Return the association this end attaches to the class
    * @return the association this end is part of
    */
   public AbstractAssociation getAssociation();
}
