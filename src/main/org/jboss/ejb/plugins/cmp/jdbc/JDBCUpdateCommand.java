/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.PreparedStatement;

/**
 * Template Method [Gamma et. al, 1995] which defines the basic process of 
 * executing an update statement and handling the results. Implementations 
 * just need to override <code>handleResult</code>.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.5 $
 */
public abstract class JDBCUpdateCommand extends JDBCCommand {
   // Constructors --------------------------------------------------
   
   protected JDBCUpdateCommand(JDBCStoreManager manager, String name) {
      super(manager, name);
   }
      
   // Protected -----------------------------------------------------
   
   /**
    * Template Method that executes the PreparedStatement and calls
    * <code>handleResult</code> on the integer result.
    *
    * @param stmt the prepared statement, with its parameters already set.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return the result from <code>handleResult</code>.
    * @throws Exception if execution or result handling fails.
    */
   protected Object executeStatementAndHandleResult(
               PreparedStatement ps,
               Object argOrArgs)
         throws Exception {
   
      int rowsAffected = ps.executeUpdate();
   
      log.debug("Rows affected = " + rowsAffected);
   
      return handleResult(rowsAffected, argOrArgs);
   }
   
   /**
    * Handle the result of successful execution of the update.
    
    * @param rs the result set from the query.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return any result needed by the subclass <code>execute</code>.
    * @throws Exception if result handling fails.
    */
   protected abstract Object handleResult(int rowsAffected, Object argOrArgs) 
         throws Exception;
}
