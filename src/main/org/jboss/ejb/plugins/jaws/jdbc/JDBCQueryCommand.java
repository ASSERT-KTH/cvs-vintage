/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jboss.logging.Logger;


/**
 * Abstract superclass for all JAWS Commands that issue JDBC queries
 * directly.
 * Provides a Template Method implementation for
 * <code>executeStatementAndHandleResult</code>.
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public abstract class JDBCQueryCommand extends JDBCCommand
{
   // Constructors --------------------------------------------------
   
   /**
    * Pass the arguments on to the superclass constructor.
    */
   protected JDBCQueryCommand(JDBCCommandFactory factory, String name)
   {
      super(factory, name);
   }
   
   // Protected -----------------------------------------------------
   
   /**
    * Template Method that executes the PreparedStatement and calls
    * <code>handleResult</code> on the resulting ResultSet.
    */
   protected void executeStatementAndHandleResult(PreparedStatement stmt)
      throws Exception
   {
      ResultSet rs = null;
      try
      {
         rs = stmt.executeQuery();
         handleResult(rs);
      } finally
      {
         if (rs != null)
         {
            try
            {
               rs.close();
            } catch (SQLException e)
            {
               Logger.exception(e);
            }
         }
      }
   }
   
   /**
    * Handle the result of successful execution of the query.
    */
   protected abstract void handleResult(ResultSet rs) throws Exception;
}
