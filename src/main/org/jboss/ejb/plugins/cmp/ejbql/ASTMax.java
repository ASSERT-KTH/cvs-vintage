/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.ejbql;


/**
 * This abstract syntax node represents MAX function.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.7 $
 */
public final class ASTMax
   extends AggregateFunction
   implements SelectFunction
{
   public ASTMax(int id)
   {
      super(id);
   }

   public Object jjtAccept(JBossQLParserVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }
}
