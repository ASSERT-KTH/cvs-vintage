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

   public Object visit(CrossJoin join, Object param)
   {
      Query newQuery = (Query) param;

      // process the left hand side of the join
      Relation left = (Relation) join.getLeft().accept(this, param);
      newQuery.setRelation(left);

      Relation right = join.getRight();
      if (right instanceof CollectionRelation)
      {
         left = (Relation) right.accept(this, newQuery);
      }
      else
      {
         left = new CrossJoin(left, (Relation) right.accept(this, param));
         newQuery.setRelation(left);
      }
      return left;
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      Query newQuery = (Query) param;
      Path path = relation.getPath();
      NamedRelation left = addJoinsFromPath(newQuery, path);

      RangeRelation right = new RangeRelation(relation.getAlias(), relation.getType());
      newQuery.addAlias(right);

      addJoin(newQuery, left, right, (AbstractAssociationEnd) path.getLastStep());
      return newQuery.getRelation();
   }

   public Object visit(Path path, Object param)
   {
      Query newQuery = (Query) param;
      Object lastStep = path.getLastStep();
      if (lastStep == null)
      {
         return new Path(path.getRoot());
      }
      else
      {
         NamedRelation left = addJoinsFromPath(newQuery, path);
         Path newPath = new Path(left);
         if (lastStep instanceof AbstractAttribute)
            newPath.addStep((AbstractAttribute) lastStep);
         else
            newPath.addStep((AbstractAssociationEnd) lastStep);
         return newPath;
      }
   }

   private NamedRelation addJoinsFromPath(Query query, Path path)
   {
      NamedRelation left = path.getRoot();
      RangeRelation right;

      StringBuffer pathAlias = new StringBuffer(left.getAlias());
      for (Iterator i = path.listSteps(); i.hasNext();)
      {
         Object o = i.next();
         if (i.hasNext())
         {
            AbstractAssociationEnd step = (AbstractAssociationEnd) o;
            pathAlias.append('_').append(step.getName());
            String alias = pathAlias.toString();
            right = (RangeRelation) query.getRelation(alias);
            if (right == null)
            {
               right = new RangeRelation(alias, step.getPeer().getType());
               query.addAlias(right);
               addJoin(query, left, right, step);
            }
            left = right;
         }
      }
      return left;
   }

   private Relation addJoin(Query query, NamedRelation left, NamedRelation right, AbstractAssociationEnd end)
   {
      Relation joinTree = query.getRelation();
      if (joinTree == null)
      {
         joinTree = right;
         JoinCondition joinCondition = new JoinCondition(left, right, end);
         Condition filter = query.getFilter();
         if (filter == null)
         {
            filter = joinCondition;
         }
         else
         {
            ConditionExpression newFilter = new ConditionExpression(ConditionExpression.AND);
            newFilter.addChild(filter);
            newFilter.addChild(joinCondition);
            filter = newFilter;
         }
         query.setFilter(filter);
      }
      else
      {
         joinTree = new InnerJoin(joinTree, left, right, end);
      }
      query.setRelation(joinTree);
      return joinTree;
   }

}
