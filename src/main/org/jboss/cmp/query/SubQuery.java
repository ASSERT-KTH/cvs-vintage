/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

public class SubQuery extends Query
{
   private final Query parent;

   public SubQuery(Query parent)
   {
      this.parent = parent;
   }

   public Query getQuery()
   {
      return parent;
   }

   public NamedRelation getRelation(String alias)
   {
      NamedRelation relation = super.getRelation(alias);
      if (relation == null)
         relation = parent.getRelation(alias);
      return relation;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
