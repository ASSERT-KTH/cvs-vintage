
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.ejb.plugins.local;

import org.jboss.ejb.Container;
import org.jboss.proxy.Interceptor;
import org.jboss.invocation.Invocation;
/**
 * ContainerInterceptor.java is an adapter between the client interceptor stack and the Container.
 *
 *
 * Created: Mon Feb  3 23:12:41 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class ContainerInterceptor extends Interceptor {

   private final Container container;

   public ContainerInterceptor(Container container)
   {
      this.container = container;   
   }

   public Object invoke(Invocation invocation) throws Exception
   {
      return container.invoke(invocation);
   }
   
}// ContainerInterceptor
