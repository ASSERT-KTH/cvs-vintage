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

public class Expression extends BaseNode
{
   protected AbstractType type;

   public Expression(AbstractType type)
   {
      this.type = type;
   }

   /**
    * Return the AbstractType of the result of this expression.
    * @return the type of the result of this expression
    */
   public AbstractType getType()
   {
      return type;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }

}
