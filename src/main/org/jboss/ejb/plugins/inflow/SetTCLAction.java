package org.jboss.ejb.plugins.inflow;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SetTCLAction implements PrivilegedAction
{
   Thread t;
   ClassLoader loader;

   SetTCLAction(Thread t, ClassLoader loader)
   {
      this.t = t;
      this.loader = loader;
   }
   public Object run()
   {
      t.setContextClassLoader(loader);
      loader = null;
      return null;
   }

   static void setContextClassLoader(Thread t, ClassLoader loader)
   {
      SetTCLAction action = new SetTCLAction(t, loader);
      AccessController.doPrivileged(action);
   }

}

