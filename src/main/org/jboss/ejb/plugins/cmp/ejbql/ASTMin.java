/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.ejbql;


/**
 * This abstract syntax node represents MIN function.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.6 $
 */
public final class ASTMin
   extends AggregateFunction
   implements SelectFunction
{
   public ASTMin(int id)
   {
      super(id);
   }

   /** Accept the visitor. **/
   public Object jjtAccept(JBossQLParserVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }
}
