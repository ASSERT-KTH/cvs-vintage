/*
 * JBoss, the OpenSource J2EE webOS
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
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *	@version $Revision: 1.8 $
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

