/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp12.interfaces;

import  org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.3 $
 */
public class EntityProxy
	extends org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy
   implements org.jboss.proxy.InvocationHandler
{
   public EntityProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
		super(name, container, id, optimize);
   }
}

