/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.ejbql;

import org.jboss.logging.Logger;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCResultSetReader;

import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * This abstract syntax node represents an ABS function.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.2 $
 */
public final class ASTMod
   extends SimpleNode
   implements SelectFunction
{
   private final Logger log;

   public ASTMod(int id)
   {
      super(id);
      log = Logger.getLogger(getClass());
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
      return JDBCResultSetReader.LONG_READER.get(rs, 1, Long.class, log);
   }
}
