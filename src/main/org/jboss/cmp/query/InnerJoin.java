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

/**
 * A node that represents an inner join between two relations.
 */
public class InnerJoin extends Join
{
   private AbstractAssociationEnd end;
   private NamedRelation join;

   public InnerJoin(Relation left, NamedRelation join, Relation right, AbstractAssociationEnd end)
   {
      super(left, right);
      this.join = join;
      this.end = end;
   }

   public NamedRelation getJoin()
   {
      return join;
   }

   public AbstractAssociationEnd getAssociationEnd()
   {
      return end;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
