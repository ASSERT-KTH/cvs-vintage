package org.jboss.cmp.query;

public class Assignment extends BaseNode
{
   private Path target;
   private Expression expression;

   public Assignment(Path target, Expression expression)
   {
      this.target = target;
      this.expression = expression;
   }

   public Path getTarget()
   {
      return target;
   }

   public Expression getExpression()
   {
      return expression;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
