/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.Iterator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;


import org.jboss.ejb.plugins.cmp.InitCommand;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

/**
 * JDBCInitCommand creates the table if specified in xml.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 1.1 $
 */
public class JDBCInitCommand extends JDBCUpdateCommand implements InitCommand {
	
   // Constructors --------------------------------------------------
	public JDBCInitCommand(JDBCStoreManager manager) {
		super(manager, "Init");

		// Create table SQL
		StringBuffer sql = new StringBuffer();
	   sql.append("CREATE TABLE ").append(entityMetaData.getTableName());
		
		sql.append(" (");
			// list all cmp fields
			sql.append(SQLUtil.getCreateTableColumnsClause(entity.getJDBCCMPFields()));
			
			// If there is a primary key field,
			// and the bean has explicitly <pk-constraint>true</pk-constraint> in jbosscmp-jdbc.xml
			// add primary key constraint.
			if(entityMetaData.hasPrimaryKeyConstraint())  {
				sql.append(", CONSTRAINT pk").append(entityMetaData.getTableName());
				
				sql.append(" PRIMARY KEY (");
					sql.append(SQLUtil.getColumnNamesClause(entity.getJDBCPrimaryKeyFields()));
				sql.append(")");
			}	
		sql.append(")");
		
		setSQL(sql.toString());
   }

   // InitCommand implementation ---------------------------------

   public void execute() throws Exception {
		// Create table if necessary
		if(entityMetaData.getCreateTable()) {
			// first check if the table already exists...
			// (a j2ee spec compatible jdbc driver has to fully 
			// implement the DatabaseMetaData)
			Connection con = null;
			ResultSet rs = null;
			try {
				con = manager.getConnection();
				DatabaseMetaData dmd = con.getMetaData();
				rs = dmd.getTables(con.getCatalog(), null, entityMetaData.getTableName(), null);
				if (rs.next ()) {
					log.log("Table '" + entityMetaData.getTableName() + "' already exists");
					return;
				}
			} finally {
				JDBCUtil.safeClose(rs);
				JDBCUtil.safeClose(con);
			}

			try {
				// since we use the pools, we have to do this within a transaction
				manager.getContainer().getTransactionManager().begin ();
				jdbcExecute(null);
				manager.getContainer().getTransactionManager().commit ();
				
				// Create successful, log this
				log.log("Created table '" + entityMetaData.getTableName() + "' successfully.");
			 } catch (Exception e) {
				log.debug("Could not create table " + entityMetaData.getTableName() + ": " + e.getMessage());
				try {
					manager.getContainer().getTransactionManager().rollback ();
				} catch (Exception _e) {
					log.error("Could not roll back transaction: "+ _e.getMessage());
				}
         }
      }
   }

   // JDBCUpdateCommand overrides -----------------------------------

   protected Object handleResult(int rowsAffected, Object argOrArgs) throws Exception {
      log.debug("Table " + entityMetaData.getTableName() + " created");
      return null;
   }
}
