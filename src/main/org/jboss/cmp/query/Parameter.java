/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;


public class Parameter extends Expression
{
   private int index;

   public Parameter(CommandNode command, int index)
   {
      super(command.getParameters()[index]);
      this.index = index;
   }

   public int getIndex()
   {
      return index;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
