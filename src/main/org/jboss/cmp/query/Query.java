/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import org.jboss.cmp.schema.AbstractType;

/**
 * Class used to represent a Query against a schema. This represents the query
 * in terms of basic relational operators: relations, projections and filters.
 * This can be manipulated to change the behaviour of the query, or to map
 * a abstract query down to operations supported by the underlying store.
 */
public class Query extends BaseQueryNode implements CommandNode
{
   private AbstractType[] parameters;

   public Query(AbstractType[] parameters)
   {
      this.parameters = parameters;
   }

   public AbstractType[] getParameters()
   {
      return parameters;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
