/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy;

import java.io.Externalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.PayloadKey;

/**
 * An invocation handler whichs sets up the client invocation and
 * starts the invocation interceptor call chain.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.14 $
 */
public class ClientContainer
   implements Externalizable, InvocationHandler
{
   /** The serialVersionUID. @since 1.5 */
   private static final long serialVersionUID = -4061374432170701306L;

   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};

   /**
    * The <em>static</em> information that gets attached to every invocation. 
    */ 
   public InvocationContext context;
   
   /** The first interceptor in the chain. */
   public Interceptor next;
   
   /**
    * Exposed for externalization.
    */
   public ClientContainer()
   {
      super();
   }
   
   public ClientContainer(final InvocationContext context) 
   {
      this.context = context;
   }
   
   public Object invoke(final Object proxy,
                        final Method m,
                        Object[] args)
      throws Throwable
   {
      // Normalize args to always be an array
      // Isn't this a bug in the proxy call??
      if (args == null)
         args = EMPTY_ARGS;

      // Create the invocation object
      Invocation invocation = new Invocation();
      
      // Contextual information for the interceptors
      invocation.setInvocationContext(context);
      invocation.setId(context.getCacheId());
      invocation.setObjectName(context.getObjectName());
      invocation.setMethod(m);
      invocation.setArguments(args);
      invocation.setValue(InvocationKey.INVOKER_PROXY_BINDING,
                          context.getInvokerProxyBinding(),
                          PayloadKey.AS_IS);

      // send the invocation down the client interceptor chain
      Object obj = next.invoke(invocation);
      return obj;
   }

   public InvocationContext getInvocationContext()
   {
      return this.context;
   }
   public ArrayList getInterceptors()
   {
      ArrayList tmp = new ArrayList();
      Interceptor inext = next;
      while( inext != null )
      {
         tmp.add(inext);
         inext = inext.nextInterceptor;
      }
      return tmp;
   }
   public void setInterceptors(ArrayList interceptors)
   {
      if( interceptors.size() == 0 )
         return;
      next = (Interceptor) interceptors.get(0);
      Interceptor i = next;
      for(int n = 1; n < interceptors.size(); n ++)
      {
         Interceptor inext = (Interceptor) interceptors.get(n);
         i.setNext(inext);
         i = inext;
      }
   }

   public Interceptor setNext(Interceptor interceptor) 
   {
      next = interceptor;
      
      return interceptor;
   }
   
   /**
    * Externalization support.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(next);
      out.writeObject(context);
   }

   /**
    * Externalization support.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      next = (Interceptor) in.readObject();
      context = (InvocationContext) in.readObject();
   }
}
 
