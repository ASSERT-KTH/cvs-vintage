/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
/**
 * JDBCFindAllCommand automatic finder used in CMP 1.x.  This should 
 * be disabled for 2.x.  I will finish this command in CMP 2.x phase 3.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public class JDBCFindAllCommand extends JDBCFinderCommand {
	// Constructors --------------------------------------------------
	
	public JDBCFindAllCommand(JDBCStoreManager manager, JDBCQueryMetaData q) {
		super(manager, q);
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(entity.getJDBCPrimaryKeyFields()));
		sql.append(" FROM ").append(entityMetaData.getTableName());
		
		setSQL(sql.toString());
	}
}
