/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

public class ConditionExpression extends Condition
{
   public static final String AND = "AND";
   public static final String OR = "OR";
   public static final String NOT = "NOT";
   private final String operator;

   public ConditionExpression(String operator)
   {
      this.operator = operator;
   }

   public String getOperator()
   {
      return operator;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
