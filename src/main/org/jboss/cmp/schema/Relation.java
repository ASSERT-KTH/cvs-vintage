/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.schema;

import java.util.Map;

/**
 * Base class for Relations that describe collections of tuples.
 */
public abstract class Relation
{
   private final String name;

   protected Relation(String name)
   {
      this.name = name;
   }

   /**
    * Return the alias name associated with this Relation
    * @return the alias for this Relation
    */
   public String getName()
   {
      return name;
   }

   /**
    * Return the AbstractClass describing the tuples comprising this relation
    * @return the AbstractClass that this Relations comprises
    */
   public abstract AbstractClass getType();

   /**
    * Map this relation to another schema
    * @param schemaMap the map from this Relation's schema to the other
    * @param relationMap the map of Relations in this Query to their
    *                    equivilent in the other schema
    * @return this relation mapped to the other schema
    */
   public abstract Relation mapSchema(Map schemaMap, Map relationMap);
}
