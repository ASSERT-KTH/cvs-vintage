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
 * The AbstractSchema interface allows generic access to static data models
 * by different clients. This is the root of a mete-model (loosely based on
 * UML that can be used to represent diverse systems. <br />
 * <br />
 * The model is build up from the following interfaces:
 * <ul>
 * <li>AbstractType - a primitive data type</li>
 * <li>AbstractClass - a structured data type</li>
 * <li>AbstractAttribute - a member of a structured data type</li>
 * <li>AbstractAssociation with AbstractAssociationEnd(s) -
 *     links between AbstractClasses</li>
 * </ul>
 */
public interface AbstractSchema
{
   /**
    * Locate the AbstractClass given its unique name.
    * @param name the name of the class
    * @return the AbstractClass with the given name, or null if not found
    */
   public AbstractClass getClassByName(String name);

   /**
    * Check whether the given name is considered used. This may search a
    * larger namespace than getClassByName allowing for alternative named
    * and reserved identifiers.
    * @param name the name to check
    * @return true if the schema considers the name to be used
    */
   public boolean isClassNameInUse(String name);
}
