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
import org.jboss.cmp.query.CrossJoin;
import org.jboss.cmp.query.NamedRelation;
import org.jboss.cmp.query.Path;
import org.jboss.cmp.query.Projection;
import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.QueryNode;
import org.jboss.cmp.query.QueryVisitor;
import org.jboss.cmp.query.RangeRelation;
import org.jboss.cmp.query.InnerJoin;

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
         NamedRelation root = path.getRoot();
         String alias = root.getAlias();
         String[] pkColumns = ((Table)root.getType()).getPkFields();
         for (int i = 0; i < pkColumns.length; i++)
         {
            if (i != 0)
            {
               buf.append(",");
            }
            String pkColumn = pkColumns[i];
            buf.append(" ").append(alias).append(".").append(pkColumn);
         }
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
      buf.append(" ").append(relation.getType().getName());
      buf.append(" ").append(relation.getAlias());
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
      buf.append(" CROSS JOIN");
      join.getRight().accept(this, buf);
      return buf;
   }

   public Object visit(InnerJoin join, Object param)
   {
      StringBuffer buf = (StringBuffer) param;
      join.getLeft().accept(this, buf);
      buf.append(" INNER JOIN");
      join.getRight().accept(this, buf);
      buf.append(" ON (...)");
      return buf;
   }
}
