/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.sql.Types;

import org.w3c.dom.Element;

import org.jboss.logging.Logger;
import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;

/**
 *      
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCMappingMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	
	private String javaType;
	
	private int jdbcType;
	
	private String sqlType;
	
	
	// Static --------------------------------------------------------
	
	public static int getJdbcTypeFromName(String name) throws DeploymentException {		
		if (name == null) {
			throw new DeploymentException("jdbc-type cannot be null");
		}
		
		try {
			Integer constant = (Integer)Types.class.getField(name).get(null);
			return constant.intValue();
		
		} catch (Exception e) {
			Logger.warning("Unrecognized jdbc-type: " + name + ", using Types.OTHER");
			return Types.OTHER;
		}
	} 
	
	
	// Constructors --------------------------------------------------
   
	// Public --------------------------------------------------------
    
	public String getJavaType() {
		return javaType;
	}
	
	public int getJdbcType() {
		return jdbcType;
	}
	
	public String getSqlType() {
		return sqlType;
	}	
	
	// XmlLoadable implementation ------------------------------------
	
	public void importXml(Element element) throws DeploymentException {
		
		javaType = getElementContent(getUniqueChild(element, "java-type"));
		
		jdbcType = getJdbcTypeFromName(getElementContent(getUniqueChild(element, "jdbc-type")));
		
		sqlType = getElementContent(getUniqueChild(element, "sql-type"));
	}	
	
	// Package protected ---------------------------------------------
	
	// Protected -----------------------------------------------------
	
	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------
}
