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
 * Interface to be implemented by Visitors who wish to traverse a Query's nodes
 */
public interface QueryVisitor
{
   public Object visit(Query query, Object param);

   public Object visit(SubQuery subquery, Object param);

   public Object visit(Projection projection, Object param);

   public Object visit(Path path, Object param);

   public Object visit(RangeRelation relation, Object param);

   public Object visit(CollectionRelation relation, Object param);

   public Object visit(CrossJoin join, Object param);

   public Object visit(InnerJoin join, Object param);

   public Object visit(Comparison comparison, Object param);

   public Object visit(JoinCondition joinCondition, Object param);

   public Object visit(ConditionExpression expression, Object param);

   public Object visit(IsNull expression, Object param);

   public Object visit(Exists expression, Object param);

   public Object visit(Literal literal, Object param);

   public Object visit(Parameter queryParam, Object param);
}
