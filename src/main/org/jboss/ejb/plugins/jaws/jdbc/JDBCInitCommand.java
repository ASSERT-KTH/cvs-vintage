/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.util.Iterator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.jboss.ejb.plugins.jaws.JPMInitCommand;
import org.jboss.ejb.plugins.jaws.CMPFieldInfo;
import org.jboss.ejb.plugins.jaws.MetaInfo;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * JAWSPersistenceManager JDBCInitCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
public class JDBCInitCommand
   extends JDBCUpdateCommand
   implements JPMInitCommand
{
   // Constructors --------------------------------------------------

   public JDBCInitCommand(JDBCCommandFactory factory)
   {
      super(factory, "Init");

      // Create table SQL
      String sql = "CREATE TABLE " + metaInfo.getTableName() + " (";

      Iterator it = metaInfo.getCMPFieldInfos();
      boolean first = true;
      while (it.hasNext())
      {
         CMPFieldInfo fieldInfo = (CMPFieldInfo)it.next();

         if (fieldInfo.isEJBReference())
         {
            JawsCMPField[] pkFields = fieldInfo.getForeignKeyCMPFields();

            for (int i = 0; i < pkFields.length; i++)
            {
               sql += (first ? "" : ",") +
                      fieldInfo.getColumnName() + "_" +
                      pkFields[i].getColumnName() + " " +
                      pkFields[i].getSqlType();
               first = false;
            }
         } else
         {
            sql += (first ? "" : ",") +
                   fieldInfo.getColumnName() + " " +
                   fieldInfo.getSQLType();
            first = false;
         }
      }

      sql += ")";

      setSQL(sql);
   }

   // JPMInitCommand implementation ---------------------------------

   public void execute() throws Exception
   {
      // Create table if necessary
      if (metaInfo.getCreateTable())
      {
          boolean created = false;
          Connection con = null;
          Statement st = null;
          ResultSet rs = null;
          try {
              con = getConnection();
              st = con.createStatement();
              rs = st.executeQuery("SELECT COUNT(*) FROM "+metaInfo.getTableName()+" WHERE 0=1");
              if(rs.next())
                created = true;
              rs.close();
              rs = null;
              st.close();
              st = null;
              con.close();
              con = null;
          } catch(SQLException e) {
              created = false;
          } finally {
              if(rs != null) try {rs.close();}catch(SQLException e) {}
              if(st != null) try {st.close();}catch(SQLException e) {}
              if(con != null) try {con.close();}catch(SQLException e) {}
          }

         // Try to create it
         if(created) {
             System.out.println("Table '"+metaInfo.getTableName()+"' already exists!");
         } else {
             try
             {
                jdbcExecute(null);
             } catch (Exception e)
             {
                log.debug("Could not create table " +
                          metaInfo.getTableName() + ": " + e.getMessage());
             }
         }
      }
   }

   // JDBCUpdateCommand overrides -----------------------------------

   protected Object handleResult(int rowsAffected, Object argOrArgs)
      throws Exception
   {
      log.debug("Table " + metaInfo.getTableName() + " created");

      return null;
   }
}
