/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import java.util.HashMap;
import java.util.Map;

import org.jboss.cmp.schema.AbstractType;

/**
 * Class used to represent a Query against a schema. This represents the query
 * in terms of basic relational operators: relations, projections and filters.
 * This can be manipulated to change the behaviour of the query, or to map
 * a abstract query down to operations supported by the underlying store.
 */
public class Query extends BaseNode
{
   private AbstractType[] parameters;
   private Relation relation;
   private Projection projection;
   private QueryNode filter;

   private final Map aliases = new HashMap();

   public Query()
   {
   }

   public Query(AbstractType[] parameters)
   {
      this.parameters = parameters;
   }

   public void setRelation(Relation relation)
   {
      this.relation = relation;
   }

   public Relation getRelation()
   {
      return relation;
   }

   public void setProjection(Projection projection)
   {
      this.projection = projection;
   }

   public Projection getProjection()
   {
      return projection;
   }

   public AbstractType[] getParameters()
   {
      return parameters;
   }

   public void addAlias(NamedRelation relation)
   {
      aliases.put(relation.getAlias(), relation);
   }

   /**
    * Return a specific Relation identified by its alias
    * @param alias the alias used to identify the Relation
    * @return the Relation associated with the alias
    */
   public NamedRelation getRelation(String alias)
   {
      return (NamedRelation) aliases.get(alias);
   }

   public QueryNode getFilter()
   {
      return filter;
   }

   public void setFilter(QueryNode filter)
   {
      this.filter = filter;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
