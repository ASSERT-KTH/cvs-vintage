package org.jboss.cmp.query;

import org.jboss.cmp.schema.AbstractType;

public class Update extends BaseNode implements CommandNode
{
   private Condition filter;
   private AbstractType[] parameters;
   private RangeRelation relation;

   public Update(RangeRelation relation, AbstractType[] parameters)
   {
      this.relation = relation;
      this.parameters = parameters;
   }

   public Condition getFilter()
   {
      return filter;
   }

   public void setFilter(Condition filter)
   {
      this.filter = filter;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }

   public AbstractType[] getParameters()
   {
      return parameters;
   }

   public RangeRelation getRelation()
   {
      return relation;
   }

   public void setRelation(RangeRelation relation)
   {
      this.relation = relation;
   }
}
