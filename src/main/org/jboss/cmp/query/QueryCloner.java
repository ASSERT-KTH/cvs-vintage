/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/


package org.jboss.cmp.query;

import java.util.Iterator;

import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractAttribute;

/**
 * Base class which creates a deep copy of a Query. Intended to be subclassed
 * by implementations that do more useful things.
 */
public class QueryCloner implements QueryVisitor
{
   public Query cloneQuery(Query source) {
      return (Query) source.accept(this, null);
   }

   public Object visit(Query query, Object param)
   {
      Query newQuery = new Query();
      newQuery.setRelation((Relation) query.getRelation().accept(this, newQuery));
      newQuery.setProjection((Projection) query.getProjection().accept(this, newQuery));
      return newQuery;
   }

   public Object visit(Projection projection, Object param)
   {
      Projection newProjection = new Projection();
      newProjection.setDistinct(projection.isDistinct());
      for (Iterator i = projection.getChildren().iterator(); i.hasNext();)
      {
         QueryNode node = (QueryNode) i.next();
         newProjection.addChild((QueryNode) node.accept(this, param));
      }
      return newProjection;
   }

   public Object visit(Path path, Object param)
   {
      Path newPath = new Path((NamedRelation) path.getRoot().accept(this, param));
      for (Iterator i = path.listSteps(); i.hasNext();)
      {
         Object o = i.next();
         if (o instanceof AbstractAttribute)
         {
            newPath.addStep((AbstractAttribute) o);
         }
         else if (o instanceof AbstractAssociationEnd)
         {
            newPath.addStep((AbstractAssociationEnd) o);
         }
      }
      return newPath;
   }

   public Object visit(RangeRelation relation, Object param)
   {
      return new RangeRelation(relation.getAlias(), relation.getType());
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      return new CollectionRelation(relation.getAlias(), (Path) relation.getPath().accept(this, param));
   }

   public Object visit(CrossJoin join, Object param)
   {
      return new CrossJoin((Relation)join.getLeft().accept(this, param), (Relation)join.getRight().accept(this, param));
   }

   public Object visit(InnerJoin join, Object param)
   {
      return new InnerJoin(
            (Relation)join.getLeft().accept(this, param),
            (NamedRelation)join.getJoin().accept(this, param),
            (Relation)join.getRight().accept(this, param),
            join.getAssociationEnd());
   }
}
