/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for nodes in the Query processing tree
 */
public abstract class BaseNode implements QueryNode
{
   private QueryNode parent;
   private List children;

   public void setParent(QueryNode parent)
   {
      this.parent = parent;
   }

   public QueryNode getParent()
   {
      return parent;
   }

   public void addChild(QueryNode child)
   {
      if (children == null)
      {
         children = new ArrayList();
      }
      children.add(child);
   }

   public List getChildren()
   {
      if (children == null)
      {
         return Collections.EMPTY_LIST;
      }
      else
      {
         return children;
      }
   }

   public abstract Object accept(QueryVisitor visitor, Object param);
}
