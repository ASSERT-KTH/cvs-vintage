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
 * Interface allowing access to a structured data type
 */
public interface AbstractClass extends AbstractType
{
   /**
    * Return information on a member of the structured type.
    * @param name the name of the attribute
    * @return information in the given attribute
    */
   public AbstractAttribute getAttributeByName(String name);

   /**
    * Return information on how an association attaches to this class.
    * @param name the name of the association end
    * @return information on how the association attaches
    */
   public AbstractAssociationEnd getAssocationByName(String name);
}
