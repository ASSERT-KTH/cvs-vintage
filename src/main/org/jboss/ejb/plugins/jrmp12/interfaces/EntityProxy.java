/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp12.interfaces;

import  org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.8 $
 */
public class EntityProxy
    extends org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy
   implements org.jboss.proxy.InvocationHandler
{
   public EntityProxy()
   {
      // For externalization to work
   }

   public EntityProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
       super(name, container, id, optimize);
   }
}

