package org.jboss.cmp.query;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseQueryNode extends BaseNode
{
   private Relation relation;
   protected final Map aliases = new HashMap();
   private Projection projection;
   private Condition filter;

   public void setRelation(Relation relation)
   {
      this.relation = relation;
   }

   public Relation getRelation()
   {
      return relation;
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

   public void setProjection(Projection projection)
   {
      this.projection = projection;
   }

   public Projection getProjection()
   {
      return projection;
   }

   public Condition getFilter()
   {
      return filter;
   }

   public void setFilter(Condition filter)
   {
      this.filter = filter;
   }
}
