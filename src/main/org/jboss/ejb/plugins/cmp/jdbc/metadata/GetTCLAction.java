package org.jboss.ejb.plugins.cmp.jdbc.metadata;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class GetTCLAction implements PrivilegedAction
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
