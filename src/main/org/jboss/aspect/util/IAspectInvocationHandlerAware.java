/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect.util;

import org.jboss.aspect.proxy.AspectInvocationHandler;

/**
 * 
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public interface IAspectInvocationHandlerAware {
   
	void setAspectInvocationHandler(AspectInvocationHandler aspectInvocationHandler);
	
}


