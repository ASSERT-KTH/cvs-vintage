/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.lang.reflect.Method;

/**
 *	This interface is used to identify a query that will be invoked in 
 * responce to the invocation of a finder method in a home interface or
 * an ejbSelect method in a bean implementation class.
 *    
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 *	@author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@author <a href="danch@nvisia.com">danch</a>
 *	@version $Revision: 1.3 $
 */
public interface JDBCQueryMetaData {
	/**
	 * Gets the method which invokes this query.
	 * @return the Method object which invokes this query  
	 */
	public Method getMethod();
}
