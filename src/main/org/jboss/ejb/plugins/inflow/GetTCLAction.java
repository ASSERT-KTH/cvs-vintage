package org.jboss.ejb.plugins.inflow;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class GetTCLAction implements PrivilegedAction
{
   static PrivilegedAction ACTION = new GetTCLAction(null);
   Thread t;

   GetTCLAction(Thread t)
   {
      this.t = t;
   }
   public Object run()
   {
      Thread thread = t;
      if (thread == null)
         thread = Thread.currentThread();
      ClassLoader loader = thread.getContextClassLoader();
      return loader;
   }

   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(ACTION);
      return loader;
   }
   static ClassLoader getContextClassLoader(Thread t)
   {
      GetTCLAction action = new GetTCLAction(t);
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(action);
      return loader;
   }

}

