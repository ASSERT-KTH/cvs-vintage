/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

/**
 * Abstract join between two relations.
 */
public abstract class Join extends Relation
{
   protected Relation left;
   protected Relation right;

   public Join(Relation left, Relation right)
   {
      this.left = left;
      this.right = right;
   }

   public Relation getLeft()
   {
      return left;
   }

   public Relation getRight()
   {
      return right;
   }
}
