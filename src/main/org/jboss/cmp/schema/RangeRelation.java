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
 * A basic Relation covering all instances of a specific class.
 */
public class RangeRelation extends Relation
{
   private final AbstractClass clazz;

   /**
    * Constructor for this Relation
    * @param name the alias name of this relation
    * @param clazz the AbstractClass that this relation spans
    */
   public RangeRelation(String name, AbstractClass clazz)
   {
      super(name);
      this.clazz = clazz;
   }

   public AbstractClass getType()
   {
      return clazz;
   }

   public Relation mapSchema(Map schemaMap, Map relationMap)
   {
      Relation newRelation = new RangeRelation(getName(), (AbstractClass) schemaMap.get(clazz));
      relationMap.put(this, newRelation);
      return newRelation;
   }
}
