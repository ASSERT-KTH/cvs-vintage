/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc.informix;

import java.sql.PreparedStatement;

import com.informix.jdbc.IfxStatement;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCAbstractVendorCreateCommand;

/**
 * JDBCInformixCreateCommand executes an INSERT INTO query.
 * The command uses getSerial method from Informix native
 * Statement interface implementation to fetch the generated key.
 * It works under JDK versions 1.3 and 1.4.
 *
 * @author <a href="mailto:julien_viet@yahoo.fr">Julien Viet</a>
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 1.1 $
 */
public class JDBCInformixCreateCommand
   extends JDBCAbstractVendorCreateCommand
{

   protected Object fetchPK(PreparedStatement statement) throws Exception
   {
      // cast to the wrapped statement
      org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement ws =
         (org.jboss.ejb.plugins.cmp.jdbc.WrappedStatement) statement;
      // fetch underlying statement and cast it to Informix native
      IfxStatement ifxStmt = (IfxStatement) ws.getUnderlyingStatement();

      int serialValue = ifxStmt.getSerial();
      Object pk = null;
      if (serialValue != 0) {
          pk = new Integer(serialValue);
      }
      return pk;
   }

}
