/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;
import org.w3c.dom.Element;

import org.jboss.metadata.QueryMetaData;

/**
 *      
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCQlQueryMetaData extends JDBCQueryMetaData {
	private String ejbQl;
	
	public JDBCQlQueryMetaData(QueryMetaData queryMetaData, Method method, JDBCEntityMetaData entity) {
		super(method, entity);
		ejbQl = queryMetaData.getEjbQl();
	}

	public JDBCQlQueryMetaData(JDBCQueryMetaData jdbcQueryMetaData, Element queryElement, Method method, JDBCEntityMetaData entity) {
		super(method, entity);
		if(jdbcQueryMetaData instanceof JDBCQlQueryMetaData) {
			ejbQl = ((JDBCQlQueryMetaData)jdbcQueryMetaData).getEjbQl();
		}
		// other stuff here
	}
	
	public String getEjbQl() {
		return ejbQl;
	}
}
