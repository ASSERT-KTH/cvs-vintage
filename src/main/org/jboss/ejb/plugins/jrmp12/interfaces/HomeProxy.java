/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp12.interfaces;

import  org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;
import javax.ejb.EJBMetaData;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *	@version $Revision: 1.6 $
 */
public class HomeProxy
   extends org.jboss.ejb.plugins.jrmp.interfaces.HomeProxy
   implements org.jboss.proxy.InvocationHandler
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

