/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * Template Method [Gamma et. al, 1995] which defines the basic process of 
 * executing query and handling the results. Implementations just need
 * to override <code>handleResult</code>.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
public abstract class JDBCQueryCommand extends JDBCCommand
{
   // Constructors --------------------------------------------------
   
   protected JDBCQueryCommand(JDBCStoreManager manager, String name) {
      super(manager, name);
   }
   
   // Protected -----------------------------------------------------
   
   /**
    * Template Method that executes the PreparedStatement and calls
    * <code>handleResult</code> on the resulting ResultSet.
    *
    * @param stmt the prepared statement, with its parameters already set.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return any result produced by the handling of the result of executing
    *  the prepared statement.
    * @throws Exception if execution or result handling fails.
    */
   protected Object executeStatementAndHandleResult(
                  PreparedStatement ps,
                  Object argOrArgs)
      throws Exception
   {
      ResultSet rs = null;      
      try {
         rs = ps.executeQuery();
         return handleResult(rs, argOrArgs);
      } finally  {
         JDBCUtil.safeClose(rs);
      }
   }
   
   /**
    * Handles the result of successful execution of the query.
    *
    * @param rs the result set from the query.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return any result produced by the handling of the result of executing
    *  the prepared statement.
    * @throws Exception if execution or result handling fails.
    */
   protected abstract Object handleResult(ResultSet rs, Object argOrArgs) throws Exception;
}
