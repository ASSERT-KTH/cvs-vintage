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
 *	@author Rickard �berg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public final class StatefulSessionProxy
   extends org.jboss.ejb.plugins.jrmp.interfaces.StatefulSessionProxy
   implements java.lang.reflect.InvocationHandler
{
   public StatefulSessionProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
   	super(name, container, id, optimize);
   }
}

