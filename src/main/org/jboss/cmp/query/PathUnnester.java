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
import java.util.Map;
import java.util.HashMap;

import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractClass;

/**
 * A transform that unnests Paths in a query by converting them to inner joins.
 */
public class PathUnnester extends QueryCloner
{
   private Map knownRelations = new HashMap();

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
      knownRelations.clear();
      return query;
   }

   public Object visit(CrossJoin join, Object param)
   {
      Relation right = join.getRight();
      if (right instanceof CollectionRelation == false) {
         return super.visit(join, param);
      }
      Relation newLeft = (Relation)join.getLeft().accept(this, param);
      return right.accept(this, newLeft);
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      Relation joinTree = (Relation) param;
      Path path = relation.getPath();
      NamedRelation left = path.getRoot();
      RangeRelation right;

      StringBuffer newAlias = new StringBuffer(left.getAlias());
      AbstractAssociationEnd step = null;
      for (Iterator i = path.listSteps(); i.hasNext();)
      {
         step = (AbstractAssociationEnd) i.next();
         if (i.hasNext()) {
            newAlias.append('_').append(step.getName());
            String alias = newAlias.toString();
            right = (RangeRelation) knownRelations.get(alias);
            if (right == null)
            {
               right = new RangeRelation(alias, step.getPeer().getType());
               joinTree = new InnerJoin(joinTree, left, right, step);
               knownRelations.put(alias, right);
            }
            left = right;
         }
      }
      right = new RangeRelation(relation.getAlias(), relation.getType());
      return new InnerJoin(joinTree, left, right, step);
   }
}
