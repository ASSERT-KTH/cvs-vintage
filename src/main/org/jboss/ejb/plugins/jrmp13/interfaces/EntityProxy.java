/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp13.interfaces;

import  org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;
import org.jboss.util.FastKey;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.4 $
 */
public final class EntityProxy
    extends org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy
   implements java.lang.reflect.InvocationHandler
{
   public EntityProxy()
   {
      // For externalization to work
   }
   
   public EntityProxy(String name, ContainerRemote container, FastKey id, boolean optimize)
   {
       super(name, container, id, optimize);
   }
}

