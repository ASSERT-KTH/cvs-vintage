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

import org.jboss.cmp.query.CollectionRelation;
import org.jboss.cmp.query.Comparison;
import org.jboss.cmp.query.Condition;
import org.jboss.cmp.query.ConditionExpression;
import org.jboss.cmp.query.CrossJoin;
import org.jboss.cmp.query.Expression;
import org.jboss.cmp.query.InnerJoin;
import org.jboss.cmp.query.Literal;
import org.jboss.cmp.query.NamedRelation;
import org.jboss.cmp.query.Parameter;
import org.jboss.cmp.query.Path;
import org.jboss.cmp.query.Projection;
import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.QueryNode;
import org.jboss.cmp.query.QueryVisitor;
import org.jboss.cmp.query.RangeRelation;
import org.jboss.cmp.schema.AbstractType;

/**
 * Transformer that produces (pure) SQL92 text from a Query
 */
public class SQL92Generator implements QueryVisitor
{
   /**
    * Generate SQL92 text for the supplied query. This uses standard SQL92
    * syntax without regard to a specific database's extensions or limitations.
    * @param query the Query to generate text from
    * @return SQL92 text for the query
    */
   public String generate(Query query)
   {
      StringBuffer buf = new StringBuffer(1000);
      query.accept(this, buf);
      return buf.toString();
   }

   public Object visit(Query query, Object param)
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

   public Object visit(Projection projection, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      if (projection.isDistinct()) {
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
         String[] pkColumns = ((Table)root.getType()).getPkFields();
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
      join.getLeft().accept(this, buf);
      NamedRelation right = (NamedRelation) join.getRight();
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

   public Object visit(Expression expression, Object param)
   {
      throw new UnsupportedOperationException();
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
      for (int i=0; i < s.length(); i++)
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
}
