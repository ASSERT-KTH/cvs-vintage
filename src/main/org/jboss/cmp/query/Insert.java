package org.jboss.cmp.query;

import org.jboss.cmp.schema.AbstractType;

public class Insert extends BaseNode implements CommandNode
{
   private AbstractType[] parameters;
   private RangeRelation relation;

   public Insert(RangeRelation relation, AbstractType[] parameters)
   {
      this.relation = relation;
      this.parameters = parameters;
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
}
