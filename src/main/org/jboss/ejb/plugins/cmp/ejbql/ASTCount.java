/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.ejbql;


/**
 * This abstract syntax node represents a count function.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.7 $
 */
public final class ASTCount
   extends AggregateFunction
   implements SelectFunction
{
   public ASTCount(int id)
   {
      super(id);
   }

   protected Class getDefaultResultType()
   {
      return Long.class;
   }
   
   /**
    * Accept the visitor. *
    */
   public Object jjtAccept(JBossQLParserVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }
}
