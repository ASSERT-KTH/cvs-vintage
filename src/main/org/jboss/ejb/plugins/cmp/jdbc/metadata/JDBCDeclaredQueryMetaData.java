/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

import org.jboss.ejb.DeploymentException;

import org.w3c.dom.Element;

/**
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCDeclaredQueryMetaData extends JDBCQueryMetaData {
	private String from;
	private String where;
	private String order;
	private String other;
	
	public JDBCDeclaredQueryMetaData(JDBCQueryMetaData jdbcQueryMetaData, 
					Element queryElement, 
					Method method, 
					JDBCEntityMetaData entity)
   		throws DeploymentException {
				
		super(method, entity);

		from = getElementContent(getOptionalChild(queryElement, "from"));   
		where = getElementContent(getOptionalChild(queryElement, "where"));
		order = getElementContent(getOptionalChild(queryElement, "order"));
		other = getElementContent(getOptionalChild(queryElement, "other"));
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getWhere() {
		return where;
	}
	
	public String getOrder() {
		return order;
	}
	
	public String getOther() {
		return other;
	}
}
