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
 * Interface provding access to properties of core data types.
 */
public interface AbstractType
{
   /**
    * Return the name of this type.
    * @return the name of this type
    */
   public String getName();
}
