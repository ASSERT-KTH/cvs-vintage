/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abstract superclass for all JAWS Commands that issue JDBC updates
 * directly.
 * Provides a Template Method implementation for
 * <code>executeStatementAndHandleResult</code>.
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public abstract class JDBCUpdateCommand extends JDBCCommand
{
   // Constructors --------------------------------------------------
   
   /**
    * Pass the arguments on to the superclass constructor.
    */
   protected JDBCUpdateCommand(JDBCCommandFactory factory, String name)
   {
      super(factory, name);
   }
   
   // Protected -----------------------------------------------------
   
   /**
    * Template Method that executes the PreparedStatement and calls
    * <code>handleResult</code> on the integer result.
    */
   protected void executeStatementAndHandleResult(PreparedStatement stmt)
      throws Exception
   {
      int rowsAffected = stmt.executeUpdate();
      
      if (debug)
      {
         log.debug("Rows affected = " + rowsAffected);
      }
      
      handleResult(rowsAffected);
   }
   
   /**
    * Handle the result of successful execution of the update.
    */
   protected abstract void handleResult(int rowsAffected) throws Exception;
}
