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
 * A basic Relation covering all instances of a specific class.
 */
public class RangeRelation extends NamedRelation
{

   /**
    * Constructor for this Relation
    * @param alias the alias alias of this relation
    * @param type the AbstractClass that this relation spans
    */
   public RangeRelation(String alias, AbstractClass type)
   {
      super(alias, type);
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }

}
