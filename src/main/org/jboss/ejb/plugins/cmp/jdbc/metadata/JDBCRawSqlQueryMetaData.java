/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;
import org.w3c.dom.Element;

/**
 * JDBCRawSqlQueryMetaData holds information about a raw sql query.
 * A raw sql query allows you to do anything sql allows you to do.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCRawSqlQueryMetaData extends JDBCQueryMetaData {
	public JDBCRawSqlQueryMetaData(JDBCQueryMetaData jdbcQueryMetaData, Element queryElement, Method method, JDBCEntityMetaData entity) {
		super(method, entity);
		// other stuff here
	}
}
