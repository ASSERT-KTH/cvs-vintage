/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;


import org.jboss.ejb.Container;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;

/**
 * An abstract base class for container interceptors.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.10 $
 */
public abstract class AbstractInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   /** The next interceptor in the chain. */
   protected Interceptor nextInterceptor;
   /** Use a JBoss custom log4j category for trace level logging */
   protected Logger log = Logger.getLogger(this.getClass());

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation ------------------------------------
   
   public abstract void setContainer(Container container);
   
   public abstract Container getContainer();
   
   public void setNext(final Interceptor interceptor) {
      // assert interceptor != null
      nextInterceptor = interceptor;
   }
   
   public Interceptor getNext() {
      return nextInterceptor;
   }

   public void init() throws Exception {
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

   public Object invokeHome(final MethodInvocation mi) throws Exception {
      // assert mi != null;
      return getNext().invokeHome(mi);
   }

   public Object invoke(final MethodInvocation mi) throws Exception {
      // assert mi != null;
      return getNext().invoke(mi);
   }
   
   // Protected -----------------------------------------------------
}
