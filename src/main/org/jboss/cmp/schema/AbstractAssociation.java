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
 * Interface describing the relationship between two classes, comprising
 * information about the relationship itself plus how it attaches to the
 * associated classes.
 */
public interface AbstractAssociation
{
   public static final boolean ONE = false;
   public static final boolean MANY = true;

   /**
    * Return the name of this association. This is independent of how it
    * attaches to the classes.
    * @return the name of this association
    */
   public String getName();

   /**
    * Return information on the left end of this association.
    * @return information on the left end
    */
   public AbstractAssociationEnd getLeftEnd();

   /**
    * Return information on the right end of this association.
    * @return information on the right end
    */
   public AbstractAssociationEnd getRightEnd();

   /**
    * Return a String representing, in the query language of the schema,
    * the condition used to join the two classes in a manner appropriate to
    * this association.
    * @param leftAlias the query alias for the left class
    * @param rightAlias the query alias for the right class
    * @return a comparision String in the schema's query language
    */
   public String getJoinCondition(String leftAlias, String rightAlias);
}
