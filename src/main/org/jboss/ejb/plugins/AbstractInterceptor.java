/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.util.List;

import org.jboss.ejb.Container;
import org.jboss.ejb.Interceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.logging.Logger;
import org.w3c.dom.Element;

/**
 * An abstract base class for container interceptors.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.18 $
 */
public abstract class AbstractInterceptor implements Interceptor
{
   /**
    * The container for which this interceptor is intercepting calls.
    */
   private Container container;

   /** 
    * The next interceptor in the chain. 
    */
   private Interceptor nextInterceptor;

   /** 
    * Logging instance 
    */
   protected Logger log = Logger.getLogger(this.getClass());

   /**
    * The configuration element of this interceptor.
    */
   protected Element config;

   public final void setConfiguration(Element config) 
   { 
      this.config = config;
   }

   public final void setContainer(Container container)
   {
      this.container = container;
   }
   
   public final Container getContainer()
   {
      return container;
   }
   
   public final void setNext(final Interceptor interceptor) {
      nextInterceptor = interceptor;
   }
   
   public final Interceptor getNext() {
      return nextInterceptor;
   }

   public void create() throws Exception {}
   public void start() throws Exception {}
   public void stop() {}
   public void destroy() {}

   public InvocationResponse invoke(final Invocation invocation) throws Exception {
      return getNext().invoke(invocation);
   }

   public void retrieveStatistics( List container, boolean reset ) {
      getNext().retrieveStatistics( container, reset );
   }
}
