/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.ejbql;

/**
 * @version <tt>$Revision: 1.1 $</tt>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public abstract class AggregateFunction
   extends SimpleNode
   implements SelectFunction
{
   public String distinct = "";

   public AggregateFunction(int i)
   {
      super(i);
   }
}
