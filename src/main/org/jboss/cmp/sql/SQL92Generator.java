/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import java.util.Iterator;

import org.jboss.cmp.query.Assignment;
import org.jboss.cmp.query.CollectionRelation;
import org.jboss.cmp.query.Comparison;
import org.jboss.cmp.query.Condition;
import org.jboss.cmp.query.ConditionExpression;
import org.jboss.cmp.query.CrossJoin;
import org.jboss.cmp.query.Delete;
import org.jboss.cmp.query.Exists;
import org.jboss.cmp.query.Expression;
import org.jboss.cmp.query.InnerJoin;
import org.jboss.cmp.query.Insert;
import org.jboss.cmp.query.IsNull;
import org.jboss.cmp.query.JoinCondition;
import org.jboss.cmp.query.Literal;
import org.jboss.cmp.query.NamedRelation;
import org.jboss.cmp.query.Parameter;
import org.jboss.cmp.query.Path;
import org.jboss.cmp.query.Projection;
import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.QueryNode;
import org.jboss.cmp.query.QueryVisitor;
import org.jboss.cmp.query.RangeRelation;
import org.jboss.cmp.query.Relation;
import org.jboss.cmp.query.SubQuery;
import org.jboss.cmp.query.Update;
import org.jboss.cmp.query.BaseQueryNode;
import org.jboss.cmp.query.CommandNode;
import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractType;

/**
 * Transformer that produces (pure) SQL92 text for a Query or Update
 */
public class SQL92Generator implements QueryVisitor
{
   /**
    * Generate SQL92 text for the supplied query. This uses standard SQL92
    * syntax without regard to a specific database's extensions or limitations.
    * @param query the Query to generate text from
    * @return SQL92 text for the query
    */
   public String generate(CommandNode query)
   {
      StringBuffer buf = new StringBuffer(1000);
      query.accept(this, buf);
      return buf.toString();
   }

   public Object visit(Insert insert, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      buf.append("INSERT INTO ");
      buf.append(insert.getRelation().getType().getName());
      buf.append("(");
      for (Iterator i = insert.getChildren().iterator(); i.hasNext();)
      {
         Assignment node = (Assignment) i.next();
         AbstractAttribute attr = (AbstractAttribute) node.getTarget().getLastStep();
         buf.append(attr.getName());
         if (i.hasNext())
         {
            buf.append(",");
         }
      }
      buf.append(") VALUES (");
      for (Iterator i = insert.getChildren().iterator(); i.hasNext();)
      {
         Assignment node = (Assignment) i.next();
         node.getExpression().accept(this, buf);
         if (i.hasNext())
         {
            buf.append(",");
         }
      }
      buf.append(")");
      return buf;
   }

   public Object visit(Update update, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      buf.append("UPDATE ");
      update.getRelation().accept(this, buf);
      buf.append(" SET ");
      for (Iterator i = update.getChildren().iterator(); i.hasNext();)
      {
         QueryNode node = (QueryNode) i.next();
         node.accept(this, buf);
         if (i.hasNext())
         {
            buf.append(", ");
         }
      }

      if (update.getFilter() != null)
      {
         buf.append(" WHERE ");
         update.getFilter().accept(this, buf);
      }
      return buf;
   }

   public Object visit(Delete delete, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      buf.append("DELETE FROM ");
      delete.getRelation().accept(this, buf);
      if (delete.getFilter() != null)
      {
         buf.append(" WHERE ");
         delete.getFilter().accept(this, buf);
      }
      return buf;
   }

   public Object visit(BaseQueryNode query, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      buf.append("SELECT ");
      query.getProjection().accept(this, buf);
      buf.append(" FROM ");
      query.getRelation().accept(this, buf);
      if (query.getFilter() != null)
      {
         buf.append(" WHERE ");
         query.getFilter().accept(this, buf);
      }
      return buf;
   }

   public Object visit(Query query, Object param)
   {
      return this.visit((BaseQueryNode) query, param);
   }

   public Object visit(SubQuery subquery, Object param)
   {
      return this.visit((BaseQueryNode) subquery, param);
   }

   public Object visit(Projection projection, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      if (projection.isDistinct())
      {
         buf.append("DISTINCT ");
      }
      for (Iterator i = projection.getChildren().iterator(); i.hasNext();)
      {
         QueryNode node = (QueryNode) i.next();
         node.accept(this, buf);
         if (i.hasNext())
         {
            buf.append(", ");
         }
      }
      return buf;
   }

   public Object visit(Path path, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      if (path.isCollection())
      {
         NamedRelation root = path.getRoot();
         String alias = root.getAlias();
         String[] pkColumns = ((Table) root.getType()).getPkFields();
         for (int i = 0; i < pkColumns.length; i++)
         {
            if (i != 0)
            {
               buf.append(", ");
            }
            String pkColumn = pkColumns[i];
            buf.append(alias).append('.').append(pkColumn);
         }
      }
      else
      {
         buf.append(path);
      }
      return buf;
   }

   public Object visit(RangeRelation relation, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      buf.append(relation.getType().getName());
      buf.append(' ').append(relation.getAlias());
      return buf;
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      throw new UnsupportedOperationException("SQL92 does not support CollectionRelations");
   }

   public Object visit(CrossJoin join, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      join.getLeft().accept(this, buf);
      buf.append(" CROSS JOIN ");
      join.getRight().accept(this, buf);
      return buf;
   }

   public Object visit(InnerJoin join, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      Relation left = join.getLeft();
      NamedRelation right = (NamedRelation) join.getRight();
      left.accept(this, buf);
      buf.append(" INNER JOIN ");
      right.accept(this, buf);
      RelationshipEnd end = (RelationshipEnd) join.getAssociationEnd();
      buf.append(" ON ").append(end.getJoinCondition(join.getJoin().getAlias(), right.getAlias()));
      return buf;
   }

   public Object visit(Comparison comparison, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      comparison.getLeft().accept(this, buf);
      buf.append(' ').append(comparison.getOperator()).append(' ');
      comparison.getRight().accept(this, buf);
      return buf;
   }

   public Object visit(JoinCondition joinCondition, Object param)
   {
      RelationshipEnd end = (RelationshipEnd) joinCondition.getEnd();
      StringBuffer buf = (StringBuffer) param;
      buf.append(end.getJoinCondition(joinCondition.getLeft().getAlias(), joinCondition.getRight().getAlias()));
      return buf;
   }

   public Object visit(ConditionExpression expression, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      String operator = expression.getOperator();
      Iterator i = expression.getChildren().iterator();
      Condition cond = (Condition) i.next();
      if (operator == ConditionExpression.NOT)
      {
         buf.append("NOT ");
         cond.accept(this, param);
      }
      else
      {
         buf.append("(");
         cond.accept(this, param);
         while (i.hasNext())
         {
            cond = (Condition) i.next();
            buf.append(' ').append(operator).append(' ');
            cond.accept(this, param);
         }
         buf.append(')');
      }
      return buf;
   }

   public Object visit(IsNull expression, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      Expression expr = expression.getExpr();
      if (expr instanceof Path == false)
      {
         throw new UnsupportedOperationException();
      }
      Path path = (Path) expr;
      if (path.getLastStep() instanceof AbstractAttribute)
      {
         expr.accept(this, buf);
         buf.append(expression.isNot() ? " IS NOT NULL" : " IS NULL");
      }
      else
      {
         RelationshipEnd end = (RelationshipEnd) path.getLastStep();
         buf.append(end.getIsNullCondition(expression.isNot(), path));
      }
      return buf;
   }

   public Object visit(Exists expression, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      if (expression.isNot())
         buf.append("NOT ");
      buf.append("EXISTS(");
      expression.getSubquery().accept(this, buf);
      buf.append(')');
      return buf;
   }

   public Object visit(Literal literal, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      Object o = literal.getValue();
      if (literal.getType().getFamily() == AbstractType.STRING)
      {
         escape(buf, (String) o);
      }
      else
      {
         buf.append(o);
      }
      return buf;
   }

   private void escape(StringBuffer buf, String s)
   {
      buf.append('\'');
      for (int i = 0; i < s.length(); i++)
      {
         char c = s.charAt(i);
         if (c == '\'')
            buf.append('\'');
         buf.append(c);
      }
      buf.append('\'');
   }

   public Object visit(Parameter queryParam, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      buf.append('?');
      return buf;
   }

   public Object visit(Assignment assignment, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      assignment.getTarget().accept(this, param);
      buf.append('=');
      assignment.getExpression().accept(this, param);
      return buf;
   }
}
