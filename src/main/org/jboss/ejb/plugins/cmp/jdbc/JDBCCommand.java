/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jboss.logging.Logger;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData;

/**
 * Abstract superclass for all JDBC Commands.
 * Provides a Template Method [Gamma et. al, 1995] for jdbcExecute(), default
 * implementations for some of the methods called by this template.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @version $Revision: 1.5 $
 */
public abstract class JDBCCommand {
   // Attributes ----------------------------------------------------

   protected JDBCStoreManager manager;
   protected JDBCEntityBridge entity;
   protected JDBCEntityMetaData entityMetaData;
   protected Logger log;
    
   protected String name;

   private String sql;

   // Constructors --------------------------------------------------

   /**
    * Construct a JDBCCommand with given factory and name.
    *
    * @param manager the manager with which the command is associated
    * @param name the name to be used when tracing execution.
    */
   public JDBCCommand(JDBCStoreManager manager, String name) {
      this.log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());

      this.name = name;
      
      this.manager = manager;
      entity = manager.getEntityBridge();
      entityMetaData = entity.getMetaData();
   }

   // Protected -----------------------------------------------------

   /**
    * Template method handling the mundane business of opening
    * a database connection, preparing a statement, setting its parameters,
    * executing the prepared statement, handling the result,
    * and cleaning up.
    *
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method, and passed on to 'hook' methods for
    *  getting SQL and for setting parameters.
    * @return any result produced by the handling of the result of executing
    *  the prepared statement.
    * @throws Exception if connection fails, or if any 'hook' method
    *  throws an exception.
    */
   protected Object jdbcExecute(Object argOrArgs) throws Exception {
      Connection con = null;
      PreparedStatement ps = null;
      
      try {
         // get the connection
         con = manager.getConnection();
         
         // get the sql
         String theSQL = getSQL(argOrArgs);
         log.debug(name + " command executing: " + theSQL);
         
         // get a prepared statement
         ps = con.prepareStatement(theSQL);
         
         // set the parameters
         setParameters(ps, argOrArgs);
         
         // execute the command
         return executeStatementAndHandleResult(ps, argOrArgs);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }

   /**
    * Used to set static SQL in subclass constructors.
    *
    * @param sql the static SQL to be used by this Command.
    */
   protected void setSQL(String sql) {
      log.debug(name + " SQL: " + sql);
      this.sql = sql;
   }

   /**
    * Gets the SQL to be used in the PreparedStatement.
    * The default implementation returns the <code>sql</code> field value.
    * This is appropriate in all cases where static SQL can be
    * constructed in the Command constructor.
    * Override if dynamically-generated SQL, based on the arguments
    * given to execute(), is needed.
    *
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return the SQL to use in the PreparedStatement.
    * @throws Exception if an attempt to generate dynamic SQL results in
    *  an Exception.
    */
   protected String getSQL(Object argOrArgs) throws Exception {
      return sql;
   }

   /**
    * Default implementation does nothing.
    * Override if parameters need to be set.
    *
    * @param stmt the PreparedStatement which will be executed by this Command.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @throws Exception if parameter setting fails.
    */
   protected void setParameters(PreparedStatement stmt, Object argOrArgs) throws Exception {
   }

   /**
    * Executes the PreparedStatement and handles result of successful execution.
    * This is implemented in subclasses for queries and updates.
    *
    * @param stmt the PreparedStatement to execute.
    * @param argOrArgs argument or array of arguments passed in from
    *  subclass execute method.
    * @return any result produced by the handling of the result of executing
    *  the prepared statement.
    * @throws Exception if execution or result handling fails.
    */
   protected abstract Object executeStatementAndHandleResult(
         PreparedStatement stmt,
         Object argOrArgs) throws Exception;            
}
