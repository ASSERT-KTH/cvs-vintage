/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.proxy.compiler;

import java.io.Serializable;

/**
 * A factory for creating proxy objects.
 *      
 * @author Unknown
 * @version $Revision: 1.1 $
 */
public class Proxy
{
   /**
    * Create a new proxy instance.
    */
   public static Object newProxyInstance(final ClassLoader loader,
                                         final Class[] interfaces,
                                         final InvocationHandler h)
   {
      Class[] interfaces2 = new Class[interfaces.length + 2];

      // why ???
      interfaces2[interfaces2.length - 2] = Serializable.class;
      interfaces2[interfaces2.length - 1] = Replaceable.class;
      
      for (int iter=0; iter<interfaces.length; iter++) {
         interfaces2[iter] = interfaces[iter];
      }

      // create a new proxy
      return Proxies.newTarget(loader, h, interfaces2);
   }
}

