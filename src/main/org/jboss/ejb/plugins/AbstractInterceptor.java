/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;


import org.jboss.ejb.Container;
import org.jboss.ejb.Interceptor;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;
import org.w3c.dom.Element;

/**
 * An abstract base class for container interceptors.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.13 $
 */
public abstract class AbstractInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The next interceptor in the chain. */
   protected Interceptor nextInterceptor;
   /** Logging instance */
   protected Logger log = Logger.getLogger(this.getClass());

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation ------------------------------------
   
   public void setConfiguration(Element config) 
   { 
      // complete 
   }


   public abstract void setContainer(Container container);
   
   public abstract Container getContainer();
   
   public void setNext(final Interceptor interceptor) {
      // assert interceptor != null
      nextInterceptor = interceptor;
   }
   
   public Interceptor getNext() {
      return nextInterceptor;
   }

   public void create() throws Exception {
      // empty
   }
   
   public void start() throws Exception {
      // empty
   }
   
   public void stop() {
      // empty
   }

   public void destroy() {
      // empty
   }

   public Object invokeHome(final Invocation mi) throws Exception {
      // assert mi != null;
      return getNext().invokeHome(mi);
   }

   public Object invoke(final Invocation mi) throws Exception {
      // assert mi != null;
      return getNext().invoke(mi);
   }
   
   // Protected -----------------------------------------------------
}
