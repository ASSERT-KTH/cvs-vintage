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

public class Parameter extends Expression
{
   private int index;

   public Parameter(Query query, int index)
   {
      super(query.getParameters()[index]);
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
