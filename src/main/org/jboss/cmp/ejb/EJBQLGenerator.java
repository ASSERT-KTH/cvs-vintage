/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import java.util.Iterator;

import org.jboss.cmp.query.CollectionRelation;
import org.jboss.cmp.query.Comparison;
import org.jboss.cmp.query.CrossJoin;
import org.jboss.cmp.query.InnerJoin;
import org.jboss.cmp.query.Literal;
import org.jboss.cmp.query.Path;
import org.jboss.cmp.query.Projection;
import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.QueryNode;
import org.jboss.cmp.query.QueryVisitor;
import org.jboss.cmp.query.RangeRelation;
import org.jboss.cmp.query.Expression;
import org.jboss.cmp.query.Parameter;
import org.jboss.cmp.query.ConditionExpression;

/**
 * Transformer that produces EJB-QL text of a query against a EJB schema.
 */
public class EJBQLGenerator implements QueryVisitor
{
   /**
    * Generate the EJB-QL text for the supplied query.
    * @param query the query to generate into EJB-QL
    * @return EJB-QL text of the query
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
      buf.append("SELECT");
      query.getProjection().accept(this, buf);
      buf.append(" FROM");
      query.getRelation().accept(this, buf);
      return buf;
   }

   public Object visit(Projection projection, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      if (projection.isDistinct()) {
         buf.append(" DISTINCT");
      }
      for (Iterator i = projection.getChildren().iterator(); i.hasNext();)
      {
         QueryNode node = (QueryNode) i.next();
         node.accept(this, buf);
         if (i.hasNext())
         {
            buf.append(",");
         }
      }
      return buf;
   }

   public Object visit(Path path, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      if (path.isCollection())
      {
         buf.append(" OBJECT(").append(path.getRoot().getAlias()).append(")");
      }
      else
      {
         buf.append(" ").append(path);
      }
      return buf;
   }

   public Object visit(RangeRelation relation, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      CMPEntity type = (CMPEntity) relation.getType();
      buf.append(" ").append(type.getSchemaName());
      buf.append(" ").append(relation.getAlias());
      return buf;
   }

   public Object visit(CollectionRelation relation, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      buf.append(" IN(").append(relation.getPath());
      buf.append(") ").append(relation.getAlias());
      return buf;
   }

   public Object visit(CrossJoin join, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      join.getLeft().accept(this, buf);
      buf.append(",");
      join.getRight().accept(this, buf);
      return buf;
   }

   public Object visit(InnerJoin join, Object param)
   {
      throw new UnsupportedOperationException("EJB-QL does not support inner joins");
   }

   public Object visit(Comparison comparison, Object param)
   {
      throw new UnsupportedOperationException();
   }

   public Object visit(ConditionExpression expression, Object param)
   {
      throw new UnsupportedOperationException();
   }

   public Object visit(Expression expression, Object param)
   {
      throw new UnsupportedOperationException();
   }

   public Object visit(Literal join, Object param)
   {
      throw new UnsupportedOperationException();
   }

   public Object visit(Parameter queryParam, Object param)
   {
      throw new UnsupportedOperationException();
   }
}
