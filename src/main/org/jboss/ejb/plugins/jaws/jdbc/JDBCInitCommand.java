/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.util.Iterator;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.jboss.ejb.plugins.jaws.JPMInitCommand;
import org.jboss.ejb.plugins.jaws.CMPFieldInfo;
import org.jboss.ejb.plugins.jaws.MetaInfo;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * JAWSPersistenceManager JDBCInitCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
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
         // Try to create it
         try
         {
            jdbcExecute();
         } catch (Exception e)
         {
            log.debug("Could not create table " + 
                      metaInfo.getTableName() + ": " + e.getMessage());
         }
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected void handleResult(int rowsAffected) throws Exception
   {
      log.debug("Table " + metaInfo.getTableName() + " created");
   }
}
