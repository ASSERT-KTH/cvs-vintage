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
 * Interface providing information on a member of a structured type. This may
 * in itself be another structured type (if the underlying schema allows).
 */
public interface AbstractAttribute
{
   /**
    * Return the name of this attribute
    * @return the name of this attribute
    */
   public String getName();

   /**
    * Return the type of this attribute. This may be a simple AbstractType, or
    * a more complex AbstractClass.
    * @return the type of this attribute
    */
   public AbstractType getType();
}
