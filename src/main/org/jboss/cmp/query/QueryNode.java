/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import java.util.List;

/**
 * Interface to be implemented by all nodes pertaining to Query's
 */
public interface QueryNode
{
   public void setParent(QueryNode parent);

   public QueryNode getParent();

   public void addChild(QueryNode child);

   public List getChildren();

   public Object accept(QueryVisitor visitor, Object param);
}
