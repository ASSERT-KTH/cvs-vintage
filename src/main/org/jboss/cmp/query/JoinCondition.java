/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import org.jboss.cmp.schema.AbstractAssociationEnd;

public class JoinCondition extends Condition
{
   private final NamedRelation left;
   private final NamedRelation right;
   private final AbstractAssociationEnd end;

   public JoinCondition(NamedRelation left, NamedRelation right, AbstractAssociationEnd end)
   {
      this.left = left;
      this.right = right;
      this.end = end;
   }

   public NamedRelation getLeft()
   {
      return left;
   }

   public NamedRelation getRight()
   {
      return right;
   }

   public AbstractAssociationEnd getEnd()
   {
      return end;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
