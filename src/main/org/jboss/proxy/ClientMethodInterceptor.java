/*
* JBoss, the OpenSource J2EE WebOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy;

import java.io.Externalizable;
import java.lang.reflect.Method;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.Interceptor;

/**
 * Handle toString, equals, hashCode locally on the client.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.com
 * @version $Revision: 1.3 $
 */
public class ClientMethodInterceptor extends Interceptor
   implements Externalizable
{
   /** The serialVersionUID. @since 1.1.2.1 */
   private static final long serialVersionUID = 6010013004557885014L;

   /** Handle methods locally on the client
    *
    * @param mi the invocation
    * @return the result of the invocation
    * @throws Throwable for any error
    */
   public Object invoke(Invocation mi) throws Throwable
   {
      Method m = mi.getMethod();
      String methodName = m.getName();
      // Implement local methods
      if (methodName.equals("toString"))
      {
         Object obj = getObject(mi);
         return obj.toString();
      }
      if (methodName.equals("equals"))
      {
         Object obj = getObject(mi);
         Object[] args = mi.getArguments();
         String thisString = obj.toString();
         String argsString = args[0] == null ? "" : args[0].toString();
         return new Boolean(thisString.equals(argsString));
      }
      if( methodName.equals("hashCode") )
      {
         Object obj = getObject(mi);
         return new Integer(obj.hashCode());
      }

      return getNext().invoke(mi);
   }

   /**
    * Get the object used in Object methods
    * 
    * @param mi the invocation
    * @return the object
    */
   protected Object getObject(Invocation mi)
   {
      Object cacheId = mi.getInvocationContext().getCacheId();
      if (cacheId != null)
         return cacheId;
      else
         return mi.getInvocationContext().getInvoker();
   }
   
}
