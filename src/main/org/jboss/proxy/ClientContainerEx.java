/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy;

import java.lang.reflect.Method;

import org.jboss.invocation.InvocationContext;

/** An extension of ClientContainer that allows one to access the client
 * container invocation context and interceptors.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class ClientContainerEx extends ClientContainer
   implements IClientContainer
{
   public ClientContainerEx()
   {
      super();
   }

   public ClientContainerEx(InvocationContext context)
   {
      super(context);
   }

   /**
    * Overriden to handle the IClientContainer methods
    * @param proxy
    * @param m - the proxied method
    * @param args - the proxied method args
    * @return 
    * @throws Throwable
    */ 
   public Object invoke(final Object proxy, final Method m, Object[] args)
      throws Throwable
   {
      if( m.getDeclaringClass() == IClientContainer.class )
      {
         return m.invoke(this, args);
      }
      return super.invoke(proxy, m, args);
   }
}
