/*
 * JBoss, the OpenSource J2EE webOS
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
 *	@version $Revision: 1.7 $
 */
public class StatefulSessionProxy
   extends org.jboss.ejb.plugins.jrmp.interfaces.StatefulSessionProxy
   implements org.jboss.proxy.InvocationHandler
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

