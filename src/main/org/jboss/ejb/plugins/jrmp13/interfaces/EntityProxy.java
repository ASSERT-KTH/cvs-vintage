/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp13.interfaces;

import  org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.6 $
 */
public final class EntityProxy
    extends org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy
   implements java.lang.reflect.InvocationHandler
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

