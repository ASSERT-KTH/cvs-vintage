/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp13.interfaces;

import  org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public final class HomeProxy
   extends org.jboss.ejb.plugins.jrmp.interfaces.HomeProxy
   implements java.lang.reflect.InvocationHandler
{
   public HomeProxy(String name, ContainerRemote container, boolean optimize)
   {
   	super(name, container, optimize);
   }
}

