/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.ejbql;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCResultSetReader;
import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * This abstract syntax node represents a square root function.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.5 $
 */
public final class ASTSqrt
   extends SimpleNode
   implements SelectFunction
{
   private final Logger log;

   public ASTSqrt(int id)
   {
      super(id);
      log = Logger.getLogger(getClass());
   }

   /** Accept the visitor. **/
   public Object jjtAccept(JBossQLParserVisitor visitor, Object data)
   {
      return visitor.visit(this, data);
   }

   // SelectFunction implementation

   /**
    * Reads results.
    * @param rs  the result set to read from.
    * @return  the result of the function
    * @throws SQLException
    */
   public Object readResult(ResultSet rs) throws SQLException
   {
      return JDBCResultSetReader.DOUBLE_READER.get(rs, 1, Double.class, log);
   }
}
