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
public final class StatelessSessionProxy
   extends org.jboss.ejb.plugins.jrmp.interfaces.StatelessSessionProxy
   implements java.lang.reflect.InvocationHandler
{
   public StatelessSessionProxy()
   {
      // For externalization to work
   }
   
   public StatelessSessionProxy(String name, ContainerRemote container, boolean optimize)
   {
   	super(name, container, optimize);
   }
}

