/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.util.Iterator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;


import org.jboss.ejb.plugins.jaws.JPMInitCommand;
import org.jboss.ejb.plugins.jaws.metadata.CMPFieldMetaData;
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;

/**
 * JAWSPersistenceManager JDBCInitCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @author <a href="mailto:david_jencks@earthlink.net">David Jencks</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * 
 * @version $Revision: 1.14 $
 * 
 * Revison:
 * 20010621 danch: merged patch from David Jenks - null constraint on columns.
 *    Also fixed bug where remapping column name of key field caused an invalid
 *    PK constraint to be build.
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
      String sql = "CREATE TABLE " + jawsEntity.getTableName() + " (";
      
      Iterator it = jawsEntity.getCMPFields();
      boolean first = true;
      while (it.hasNext())
      {
         CMPFieldMetaData cmpField = (CMPFieldMetaData)it.next();
         
         sql += (first ? "" : ",") +
                cmpField.getColumnName() + " " +
                cmpField.getSQLType() +
                cmpField.getNullable();
                
         
         first = false;
      }

      // If there is a primary key field,
      // and the bean has explicitly <pk-constraint>true</pk-constraint> in jaws.xml
      // add primary key constraint.
      if (jawsEntity.getPrimKeyField() != null && jawsEntity.hasPkConstraint())  
      {
         sql += ",CONSTRAINT pk"+jawsEntity.getTableName()+" PRIMARY KEY (";
         for (Iterator i = jawsEntity.getPkFields();i.hasNext();) {
            String keyCol = ((PkFieldMetaData)i.next()).getColumnName();
            sql += keyCol;
            sql += i.hasNext()?",":"";
         }
         sql +=")";
      }

      sql += ")";

      setSQL(sql);
   }

   // JPMInitCommand implementation ---------------------------------

   public void execute() throws Exception
   {
      // Create table if necessary
      if (jawsEntity.getCreateTable())
      {
         // first check if the table already exists...
         // (a j2ee spec compatible jdbc driver has to fully 
         // implement the DatabaseMetaData)
         boolean created = false;
         Connection con = null;
         ResultSet rs = null;
         try 
         {
             con = getConnection();
             DatabaseMetaData dmd = con.getMetaData();
             rs = dmd.getTables(con.getCatalog(), null, jawsEntity.getTableName(), null);
             if (rs.next ())
                created = true;
         
             rs.close ();
             con.close ();
         } 
         catch(Exception e) 
         {
            throw e;
         } 
         finally 
         {
             if(rs != null) try {rs.close(); rs = null;}catch(SQLException e) {}
             if(con != null) try {con.close();con = null;}catch(SQLException e) {}
         }

         // Try to create it
         if(created) {
             log.log("Table '"+jawsEntity.getTableName()+"' already exists");
         } else {
             try
             {          
                // since we use the pools, we have to do this within a transaction
                factory.getContainer().getTransactionManager().begin ();
                jdbcExecute(null);
                factory.getContainer().getTransactionManager().commit ();

                // Create successful, log this
                log.log("Created table '"+jawsEntity.getTableName()+"' successfully.");
                log.debug("Primary key of table '"+jawsEntity.getTableName()+"' is '"
                  +jawsEntity.getPrimKeyField()+"'.");
             } catch (Exception e)
             {
                log.debug("Could not create table " +
                          jawsEntity.getTableName() + ": " + e.getMessage());
                try
                {
                   factory.getContainer().getTransactionManager().rollback ();
                }
                catch (Exception _e)
                {
                   log.error("Could not roll back transaction: "+ _e.getMessage());
                }
             }
         }
      }
   }

   // JDBCUpdateCommand overrides -----------------------------------

   protected Object handleResult(int rowsAffected, Object argOrArgs)
      throws Exception
   {
      log.debug("Table " + jawsEntity.getTableName() + " created");
      return null;
   }
}
