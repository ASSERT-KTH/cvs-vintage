/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

public class Exists extends Condition
{
   private boolean not;
   private SubQuery subquery;

   public Exists(boolean not, SubQuery subquery)
   {
      this.not = not;
      this.subquery = subquery;
   }

   public boolean isNot()
   {
      return not;
   }

   public SubQuery getSubquery()
   {
      return subquery;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
