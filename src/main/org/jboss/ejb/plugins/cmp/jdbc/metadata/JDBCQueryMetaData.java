/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

import org.jboss.metadata.MetaData;

import org.w3c.dom.Element;

/**
 *      
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@author <a href="danch@nvisia.com">danch</a>
 *	@version $Revision: 1.1 $
 */
public class JDBCQueryMetaData extends MetaData {
	// Constants -----------------------------------------------------
    
	// Attributes ----------------------------------------------------
   protected Method method;
	protected JDBCEntityMetaData entity;
   	
	// Static --------------------------------------------------------
   
	// Constructors --------------------------------------------------
   public JDBCQueryMetaData(Method method, JDBCEntityMetaData entity) {
      this.method = method;
		this.entity = entity;
   }
   
	// Public --------------------------------------------------------
   public Method getMethod() {
		return method;
	}
		
	// Package protected ---------------------------------------------
    
	// Protected -----------------------------------------------------
    
	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
