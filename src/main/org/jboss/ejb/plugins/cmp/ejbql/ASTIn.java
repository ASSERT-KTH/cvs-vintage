/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;

/**
 * This abstract syntax node represents an in clause.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */                            
public class ASTIn extends SimpleNode {

   public boolean not;

   public ASTIn(int id) {
      super(id);
   }

   public ASTIn(EJBQLParser p, int id) {
      super(p, id);
   }

   public String toString() {
      return (not ? SQLUtil.NOT : SQLUtil.EMPTY_STRING) + SQLUtil.IN;
   }

   /** Accept the visitor. **/
   public Object jjtAccept(JBossQLParserVisitor visitor, Object data) {
      return visitor.visit(this, data);
   }
}
