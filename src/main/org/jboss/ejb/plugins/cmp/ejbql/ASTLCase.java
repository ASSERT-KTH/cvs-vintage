/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Types;

/**
 * This abstract syntax node represents a LCase function.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.2 $
 */
public class ASTLCase
   extends SimpleNode
   implements SelectFunction
{
   public ASTLCase(int id)
   {
      super(id);
   }

   public ASTLCase(EJBQLParser p, int id)
   {
      super(p, id);
   }

   /** Accept the visitor. **/
   public Object jjtAccept(JBossQLParserVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }

   /**
    * Reads results.
    * @param rs  the result set to read from.
    * @return  the result of the function
    * @throws SQLException
    */
   public Object readResult(ResultSet rs) throws SQLException
   {
      return JDBCUtil.getFunctionResult(rs, Types.VARCHAR, String.class);
   }
}
