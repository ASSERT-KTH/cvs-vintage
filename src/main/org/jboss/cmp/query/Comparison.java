/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

public class Comparison extends Condition
{
   public static final String EQUAL = "=";
   public static final String NOTEQUAL = "<>";
   public static final String LESSTHAN = "<";
   public static final String LESSEQUAL = "<=";
   public static final String GREATERTHAN = ">";
   public static final String GREATEREQUAL = ">=";

   private String operator;
   private Expression left;
   private Expression right;

   public Comparison(Expression left, String operator, Expression right)
   {
      this.operator = operator;
      this.left = left;
      this.right = right;
   }

   public String getOperator()
   {
      return operator;
   }

   public Expression getLeft()
   {
      return left;
   }

   public Expression getRight()
   {
      return right;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
