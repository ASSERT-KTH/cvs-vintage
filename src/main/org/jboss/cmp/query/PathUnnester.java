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
      Query query = (Query) nested.accept(this, null);
      return query;
   }

//   public Object visit(Query query, Object param)
//   {
//      Query newQuery = new Query();
//      newQuery.setRelation((Relation) query.getRelation().accept(this, newQuery));
//      newQuery.setProjection((Projection) query.getProjection().accept(this, newQuery));
//      return newQuery;
//   }

   public Object visit(CrossJoin join, Object param)
   {
      Query newQuery = (Query) param;

      // process the left hand side of the join
      Relation left = (Relation)join.getLeft().accept(this, param);
      newQuery.setRelation(left);

      Relation right = join.getRight();
      if (right instanceof CollectionRelation) {
         left = (Relation) right.accept(this, newQuery);
      }
      else
      {
         left = new CrossJoin(left, (Relation)right.accept(this, param));
      }
      newQuery.setRelation(left);
      return left;
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      Query newQuery = (Query) param;
      Relation joinTree = newQuery.getRelation();

      Path path = relation.getPath();
      NamedRelation left = path.getRoot();
      RangeRelation right;

      StringBuffer pathAlias = new StringBuffer(left.getAlias());
      AbstractAssociationEnd step = null;
      for (Iterator i = path.listSteps(); i.hasNext();)
      {
         step = (AbstractAssociationEnd) i.next();
         if (i.hasNext()) {
            pathAlias.append('_').append(step.getName());
            String alias = pathAlias.toString();
            right = (RangeRelation) newQuery.getRelation(alias);
            if (right == null)
            {
               right = new RangeRelation(alias, step.getPeer().getType());
               newQuery.addAlias(right);
               joinTree = new InnerJoin(joinTree, left, right, step);
            }
            left = right;
         }
      }
      right = new RangeRelation(relation.getAlias(), relation.getType());
      newQuery.addAlias(right);
      return new InnerJoin(joinTree, left, right, step);
   }

   public Object visit(RangeRelation relation, Object param)
   {
      return super.visit(relation, param);
   }

   public Object visit(Path path, Object param)
   {
      Query newQuery = (Query) param;
      Relation joinTree = newQuery.getRelation();

      NamedRelation left = path.getRoot();
      StringBuffer pathAlias = new StringBuffer(left.getAlias());
      for (Iterator i = path.listSteps(); i.hasNext();)
      {
         Object o = i.next();
         if (o instanceof AbstractAssociationEnd)
         {
            AbstractAssociationEnd step = (AbstractAssociationEnd) o;
            pathAlias.append('_').append(step.getName());
            String alias = pathAlias.toString();
            NamedRelation right = newQuery.getRelation(alias);
            if (right == null)
            {
               right = new RangeRelation(alias, step.getPeer().getType());
               newQuery.addAlias(right);
               joinTree = new InnerJoin(joinTree, left, right, step);
               newQuery.setRelation(joinTree);
            }
            left = right;
         }
         else
         {
            Path newPath = new Path(left);
            newPath.addStep((AbstractAttribute) o);
            return newPath;
         }
      }
      return new Path(left);
   }
}
