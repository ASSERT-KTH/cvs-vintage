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
 * A transform that unnests Paths in a query by converting them to inner joins.
 */
public class PathUnnester extends QueryCloner
{
   /**
    * Convert all paths in the specified query to attibute references by
    * unnesting multi-step paths and by convering CollectionRelations to
    * inner joins.
    * @param nested the query to be unnested
    * @return a query containing no multi-step paths
    */
   public Query unnest(Query nested)
   {
      return (Query) nested.accept(this, null);
   }

   public Object visit(CrossJoin join, Object param)
   {
      Relation right = join.getRight();
      if (right instanceof CollectionRelation == false) {
         return super.visit(join, param);
      }
      CollectionRelation oldRight = (CollectionRelation) right;
      RangeRelation newRight = new RangeRelation(oldRight.getAlias(), oldRight.getType());
      return new InnerJoin(join.getLeft(), newRight);
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      throw new IllegalStateException();
   }
}
