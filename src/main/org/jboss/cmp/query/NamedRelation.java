/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import org.jboss.cmp.schema.AbstractClass;

/**
 * A abstract Relation with a known alias name (allowing its members to be
 * referenced by expressions or joins).
 */
public abstract class NamedRelation extends Relation
{
   protected final String alias;
   protected final AbstractClass type;

   public NamedRelation(String alias, AbstractClass type)
   {
      this.alias = alias;
      this.type = type;
   }

   /**
    * Return the alias name associated with this Relation
    * @return the alias for this Relation
    */
   public String getAlias()
   {
      return alias;
   }

   /**
    * Return the type of tuples in this Relation
    * @return an AbstractClass whose attributes represent the tuples in the
    *         relation
    */
   public AbstractClass getType()
   {
      return type;
   }
}
