/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

/**
 * A node that represents a cross (product) join between two relations.
 */
public class CrossJoin extends Join
{
   public CrossJoin(Relation left, Relation right)
   {
      super(left, right);
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }

}
