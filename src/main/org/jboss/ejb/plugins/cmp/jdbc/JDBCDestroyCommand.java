/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jboss.ejb.plugins.cmp.DestroyCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

/**
 * JDBCDestroyCommand drops the table for this entity if specified in the xml.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
public class JDBCDestroyCommand extends JDBCUpdateCommand implements DestroyCommand {
   // Constructors --------------------------------------------------
   
   public JDBCDestroyCommand(JDBCStoreManager manager) {
      super(manager, "Destroy");
   }
   
   // DestroyCommand implementation ------------------------------
   
   public void execute() {
      if(entityMetaData.getRemoveTable()) {
			log.debug("Droping tables for entity " + entity.getEntityName());
			dropTable(entityMetaData.getTableName());

			// drop relation tables
			JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
			for(int i=0; i<cmrFields.length; i++) {
				if(!cmrFields[i].hasForeignKey() && !cmrFields[i].getRelatedCMRField().hasForeignKey()) {
					if(cmrFields[i].getRelationTableName() == null)  log.debug("Table name null for cmr field " + cmrFields[i].getFieldName());
					dropTable(cmrFields[i].getRelationTableName());
				}
			}
      }
   }
   
   public void dropTable(String tableName) {
			Connection con = null;
		ResultSet rs = null;
		try {
			con = manager.getConnection();
			DatabaseMetaData dmd = con.getMetaData();
			rs = dmd.getTables(con.getCatalog(), null, tableName, null);
			if(!rs.next()) {
				// table already deleted
				return;
			}
		} catch(SQLException e) {
			// ignore - bad driver
			return;
		} finally {
			JDBCUtil.safeClose(rs);
			JDBCUtil.safeClose(con);
		}

		try {
			// since we use the pools, we have to do this within a transaction
			manager.getContainer().getTransactionManager().begin ();
			jdbcExecute("DROP TABLE " + tableName);
			manager.getContainer().getTransactionManager().commit ();
			log.log("Dropped table '" + tableName + "' successfully.");
		} catch (Exception e) {
			log.debug("Could not drop table " + tableName + ": " + e.getMessage());
			try {
				manager.getContainer().getTransactionManager().rollback ();
			} catch (Exception _e) {
				log.error("Could not roll back transaction: "+ _e.getMessage());
			}
		}
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
	protected String getSQL(Object sql) throws Exception {
		return (String) sql;
	}
	   
   protected Object handleResult(int rowsAffected, Object argOrArgs) 
      throws Exception
   {     
      return null;
   }
}
