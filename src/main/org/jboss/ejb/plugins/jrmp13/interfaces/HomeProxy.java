/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp13.interfaces;

import  org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;
import javax.ejb.EJBMetaData;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *	@version $Revision: 1.6 $
 */
public final class HomeProxy
   extends org.jboss.ejb.plugins.jrmp.interfaces.HomeProxy
   implements java.lang.reflect.InvocationHandler
{
   public HomeProxy()
   {
      // For externalization to work
   }

   public HomeProxy(String name, EJBMetaData ejbMetaData, ContainerRemote container, boolean optimize)
   {
   	super(name, ejbMetaData, container, optimize);
   }
}

