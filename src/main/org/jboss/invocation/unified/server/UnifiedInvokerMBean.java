/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.unified.server;

import org.jboss.system.ServiceMBean;
import org.jboss.remoting.ServerInvocationHandler;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public interface UnifiedInvokerMBean extends ServiceMBean, ServerInvocationHandler
{
   public String getInvokerLocator();

}