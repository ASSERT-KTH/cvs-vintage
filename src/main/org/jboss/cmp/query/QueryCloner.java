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
   public CommandNode cloneQuery(CommandNode source)
   {
      return (CommandNode) source.accept(this, null);
   }

   public void visitChildren(QueryNode newNode, QueryNode oldNode, Object param)
   {
      for (Iterator i = oldNode.getChildren().iterator(); i.hasNext();)
      {
         QueryNode node = (QueryNode) i.next();
         newNode.addChild((QueryNode) node.accept(this, param));
      }
   }

   public Object visit(Insert insert, Object param)
   {
      Insert newInsert = new Insert(insert.getRelation(), insert.getParameters());
      visitChildren(newInsert, insert, param);
      return newInsert;
   }

   public Object visit(Update update, Object param)
   {
      Update newUpdate = new Update(update.getRelation(), update.getParameters());
      visitChildren(newUpdate, update, param);
      if (update.getFilter() != null)
         newUpdate.setFilter((Condition) update.getFilter().accept(this, param));
      return newUpdate;
   }

   public Object visit(Delete delete, Object param)
   {
      Delete newDelete = new Delete(delete.getRelation(), delete.getParameters());
      if (delete.getFilter() != null)
         newDelete.setFilter((Condition) delete.getFilter().accept(this, param));
      return newDelete;
   }

   public Object visit(Query query, Object param)
   {
      Query newQuery = new Query(query.getParameters());
      newQuery.setRelation((Relation) query.getRelation().accept(this, newQuery));
      newQuery.setProjection((Projection) query.getProjection().accept(this, newQuery));
      if (query.getFilter() != null)
         newQuery.setFilter((Condition) query.getFilter().accept(this, newQuery));
      return newQuery;
   }

   public Object visit(SubQuery subquery, Object param)
   {
      SubQuery newQuery = new SubQuery((Query) param);
      newQuery.setRelation((Relation) subquery.getRelation().accept(this, newQuery));
      newQuery.setProjection((Projection) subquery.getProjection().accept(this, newQuery));
      if (subquery.getFilter() != null)
         newQuery.setFilter((Condition) subquery.getFilter().accept(this, newQuery));
      return newQuery;
   }

   public Object visit(Projection projection, Object param)
   {
      Projection newProjection = new Projection();
      newProjection.setDistinct(projection.isDistinct());
      visitChildren(newProjection, projection, param);
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
      BaseQueryNode newQuery = (BaseQueryNode) param;
      RangeRelation newRelation = new RangeRelation(relation.getAlias(), relation.getType());
      newQuery.addAlias(newRelation);
      return newRelation;
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      BaseQueryNode newQuery = (BaseQueryNode) param;
      CollectionRelation newRelation = new CollectionRelation(relation.getAlias(), (Path) relation.getPath().accept(this, param));
      newQuery.addAlias(newRelation);
      return newRelation;
   }

   public Object visit(CrossJoin join, Object param)
   {
      return new CrossJoin((Relation) join.getLeft().accept(this, param), (Relation) join.getRight().accept(this, param));
   }

   public Object visit(InnerJoin join, Object param)
   {
      return new InnerJoin(
            (Relation) join.getLeft().accept(this, param),
            (NamedRelation) join.getJoin().accept(this, param),
            (Relation) join.getRight().accept(this, param),
            join.getAssociationEnd());
   }

   public Object visit(Comparison comparison, Object param)
   {
      String operator = comparison.getOperator();
      Expression left = (Expression) comparison.getLeft().accept(this, param);
      Expression right = (Expression) comparison.getRight().accept(this, param);
      return new Comparison(left, operator, right);
   }

   public Object visit(JoinCondition joinCondition, Object param)
   {
      NamedRelation left = (NamedRelation) joinCondition.getLeft().accept(this, param);
      NamedRelation right = (NamedRelation) joinCondition.getRight().accept(this, param);
      return new JoinCondition(left, right, joinCondition.getEnd());
   }

   public Object visit(ConditionExpression expression, Object param)
   {
      ConditionExpression newExpression = new ConditionExpression(expression.getOperator());
      visitChildren(newExpression, expression, param);
      return newExpression;
   }

   public Object visit(IsNull expression, Object param)
   {
      return new IsNull(expression.isNot(), (Expression) expression.getExpr().accept(this, param));
   }

   public Object visit(Exists expression, Object param)
   {
      return new Exists(expression.isNot(), (SubQuery) expression.getSubquery().accept(this, param));
   }

   public Object visit(Literal literal, Object param)
   {
      return new Literal(literal.getType(), literal.getValue());
   }

   public Object visit(Parameter queryParam, Object param)
   {
      return new Parameter((CommandNode) param, queryParam.getIndex());
   }

   public Object visit(Assignment assignment, Object param)
   {
      return new Assignment(assignment.getTarget(), assignment.getExpression());
   }
}
