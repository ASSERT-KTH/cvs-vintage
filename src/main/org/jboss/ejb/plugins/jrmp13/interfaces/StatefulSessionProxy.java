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
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.5 $
 */
public final class StatefulSessionProxy
   extends org.jboss.ejb.plugins.jrmp.interfaces.StatefulSessionProxy
   implements java.lang.reflect.InvocationHandler
{
   public StatefulSessionProxy()
   {
      // For externalization to work
   }
   
   public StatefulSessionProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
   	super(name, container, id, optimize);
   }
}

