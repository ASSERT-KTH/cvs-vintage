/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;

/**
 *	This class holds all the information jbosscmp-jdbc needs to know about a CMP field
 * It loads its data from standardjbosscmp-jdbc.xml and jbosscmp-jdbc.xml
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:vincent.harcq@hubmethods.com">Vincent Harcq</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCCMPFieldPropertyMetaData extends MetaData implements XmlLoadable {
	// Constants -----------------------------------------------------

	// Attributes ----------------------------------------------------
	
   protected String propertyName;

	// the column name in the table
	protected String columnName;

	// the jdbc type (see java.sql.Types), used in PreparedStatement.setParameter
	// default value used is intended to cause an exception if used
	protected int jdbcType = Integer.MIN_VALUE;

	// the sql type, used for table creation.
	protected String sqlType;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
	public JDBCCMPFieldPropertyMetaData(Element element) throws DeploymentException {
		// column name
		propertyName = getElementContent(getUniqueChild(element, "property-name"));

		// column name
		String columnStr = getElementContent(getOptionalChild(element, "column-name"));
		if(columnStr != null) {
			columnName = columnStr;
		} 

		// jdbc type
		String jdbcStr = getElementContent(getOptionalChild(element, "jdbc-type"));
		if(jdbcStr != null) {
			jdbcType = JDBCMappingMetaData.getJdbcTypeFromName(jdbcStr);
			sqlType = getElementContent(getUniqueChild(element, "sql-type"));
		}
	}


	// Public --------------------------------------------------------
	public String getPropertyName() {
		return propertyName;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getJDBCType() {
		return jdbcType;
	}

	public String getSQLType() {
		return sqlType;
	}

	// Package protected ---------------------------------------------

	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
