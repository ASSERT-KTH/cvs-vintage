/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

public class IsNull extends Condition
{
   private boolean not;
   private Expression expr;

   public IsNull(boolean not, Expression expr)
   {
      this.not = not;
      this.expr = expr;
   }

   public boolean isNot()
   {
      return not;
   }

   public Expression getExpr()
   {
      return expr;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
