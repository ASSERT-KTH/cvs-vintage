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
 * A Projection, or selection of members from a tuple
 */
public class Projection extends BaseNode
{
   private boolean distinct;

   /**
    * Whether this Projection returns duplicate tuples
    * @return true if the Projection will eliminate duplicate tuples
    */
   public boolean isDistinct()
   {
      return distinct;
   }

   /**
    * Set whether this Projection should eliminate duplicate tuples from its result.
    * @param distinct set true to eliminate duplicates
    */
   public void setDistinct(boolean distinct)
   {
      this.distinct = distinct;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
