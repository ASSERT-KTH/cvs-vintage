/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This abstract syntax node represents a count function.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.2 $
 */                            
public class ASTCount
   extends SimpleNode
   implements SelectFunction
{
   public ASTCount(int id) {
      super(id);
   }

   public ASTCount(EJBQLParser p, int id) {
      super(p, id);
   }


   /** Accept the visitor. **/
   public Object jjtAccept(JBossQLParserVisitor visitor, Object data) {
      return visitor.visit(this, data);
   }

   // SelectFunction implementation

   public Object readResult(ResultSet rs) throws SQLException
   {
      return JDBCUtil.getFunctionResult(rs, Types.INTEGER, Long.class);
   }
}
