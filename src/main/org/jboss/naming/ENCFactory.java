/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.util.Hashtable;
import java.util.WeakHashMap;
import java.security.PrivilegedAction;
import java.security.AccessController;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.jnp.server.NamingServer;
import org.jnp.interfaces.NamingContext;

/**
 *   Implementation of "java:comp" namespace factory. The context is associated
 *   with the thread class loader.
 *     
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 *   @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 *   @version $Revision: 1.11 $
 */
public class ENCFactory
   implements ObjectFactory
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   private static WeakHashMap encs = new WeakHashMap();
   private static ClassLoader topLoader;

   // Static --------------------------------------------------------
   public static void setTopClassLoader(ClassLoader topLoader)
   {
      ENCFactory.topLoader = topLoader;
   }
   public static ClassLoader getTopClassLoader()
   {
      return ENCFactory.topLoader;
   }


   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
      Hashtable environment)
      throws Exception
   {
      // Get naming for this component
      ClassLoader ctxClassLoader = GetTCLAction.getContextClassLoader();
      synchronized (encs)
      {
         Context compCtx = (Context) encs.get(ctxClassLoader);

         /* If this is the first time we see ctxClassLoader first check to see
          if a parent ClassLoader has created an ENC namespace.
         */
         if (compCtx == null)
         {
            ClassLoader loader = ctxClassLoader;
            GetParentAction action = new GetParentAction(ctxClassLoader);
            while( loader != null && loader != topLoader && compCtx == null )
            {
               compCtx = (Context) encs.get(loader);
               loader = action.getParent();
            }
            // If we did not find an ENC create it
            if( compCtx == null )
            {
               NamingServer srv = new NamingServer();
               compCtx = new NamingContext(environment, null, srv);
               encs.put(ctxClassLoader, compCtx);
            }
         }
         return compCtx;
      }
   }

   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
      static ClassLoader getContextClassLoader()
      {
         ClassLoader loader = (ClassLoader) AccessController.doPrivileged(ACTION);
         return loader;
      }
   }

   private static class GetParentAction implements PrivilegedAction
   {
      ClassLoader loader;
      GetParentAction(ClassLoader loader)
      {
         this.loader = loader;
      }
      public Object run()
      {
         ClassLoader parent = null;
         if( loader != null )
         {
            parent = loader.getParent();
            loader = parent;
         }
         return parent;
      }
      ClassLoader getParent()
      {
         ClassLoader parent = (ClassLoader) AccessController.doPrivileged(this);
         return parent;
      }
   }
}
