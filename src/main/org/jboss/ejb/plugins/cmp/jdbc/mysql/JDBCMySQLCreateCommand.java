/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCAbstractVendorCreateCommand;

/**
 * JDBCMySQLCreateCommand executes an INSERT INTO query.
 * The command uses getGeneratedKeys method from MySQL native
 * Statement interface implementation to fetch the generated key.
 * It works under JDK versions 1.3 and 1.4.
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 1.2 $
 */
public class JDBCMySQLCreateCommand
   extends JDBCAbstractVendorCreateCommand
{

   protected Object fetchPK(PreparedStatement statement) throws Exception
   {
      // cast to the wrapped statement
      org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement ws =
         (org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement) statement;
      // fetch underlying statement and cast it to MySQL native
      com.mysql.jdbc.PreparedStatement mySqlStmt =
         (com.mysql.jdbc.PreparedStatement) ws.getUnderlyingStatement();

      ResultSet rs = mySqlStmt.getGeneratedKeys();
      Object pk = null;
      if( rs.next() ) {
         pk = rs.getObject( 1 );
      }
      return pk;
   }

}
