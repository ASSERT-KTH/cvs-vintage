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

   public Object visit(Projection projection, Object param);

   public Object visit(Path path, Object param);

   public Object visit(RangeRelation relation, Object param);

   public Object visit(CollectionRelation relation, Object param);

   public Object visit(CrossJoin join, Object param);

   public Object visit(InnerJoin join, Object param);
}
