/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.security;

import java.security.Principal;


/**
 *	  The EJBSecurityManager is responsible for validating credentials
 *	  associated with principals.
 *      
 *   @author Daniel O'Connor docodan@nycap.rr.com
 */
public interface EJBSecurityManager
{
	public boolean isValid( Principal principal, Object credential );
}

