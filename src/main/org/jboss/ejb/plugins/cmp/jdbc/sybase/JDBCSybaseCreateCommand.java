/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc.sybase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCAbstractVendorCreateCommand;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

/**
 * JDBCCSybaseCreateCommand executes an INSERT INTO query.
 * The command uses a SELECT @@IDENTITY statement from Sybase native
 * to fetch the generated PK.
 * It works under JDK versions 1.3 and 1.4.
 *
 * @author <a href="mailto:s.m.f@softhome.net">Simone Milani-Foglia</a>
 * @author <a href="mailto:julien_viet@yahoo.fr">Julien Viet</a>
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 1.1 $
 */
public class JDBCSybaseCreateCommand
   extends JDBCAbstractVendorCreateCommand
{

   /** Informix statement that will fetch the last inserted key */
   private static final String IDENTITY_SQL = "SELECT @@IDENTITY";

   protected Object fetchPK(PreparedStatement statement) throws Exception
   {
      PreparedStatement mySybaseStmt = null;
      try
      {
         Object pk = null;
         //Retrieve the last identity created for this Connection
         mySybaseStmt = statement.getConnection().prepareStatement(IDENTITY_SQL);
         ResultSet rs = mySybaseStmt.executeQuery();

         if(rs.next())
         {
            pk = rs.getObject(1);
            log.info("Generated PK:" + pk);
         }
         return pk;
      }
      finally
      {
         JDBCUtil.safeClose(mySybaseStmt);
      }
   }

}
