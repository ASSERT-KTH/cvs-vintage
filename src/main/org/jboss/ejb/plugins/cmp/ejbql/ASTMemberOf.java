/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;

/**
 * This abstract syntax node represents a member of condition.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */                            
public class ASTMemberOf extends SimpleNode {
   public boolean not;

   public ASTMemberOf(int id) {
      super(id);
   }

   public ASTMemberOf(EJBQLParser p, int id) {
      super(p, id);
   }

   public String toString() {
      return  (not ? SQLUtil.NOT : SQLUtil.EMPTY_STRING) + SQLUtil.MEMBER_OF;
   }

   /** Accept the visitor. **/
   public Object jjtAccept(JBossQLParserVisitor visitor, Object data) {
      return visitor.visit(this, data);
   }
}
