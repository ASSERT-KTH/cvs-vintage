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

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;

/*
import javax.transaction.TransactionManager;
import java.security.Principal;
import javax.transaction.Transaction;
import javax.transaction.SystemException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

import org.jboss.proxy.ejb.ReadAheadBuffer;
import org.jboss.proxy.ejb.ListEntityProxy;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.security.SecurityAssociation;
*/

/**
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1 $
 *
 * <p><b>2001/11/19: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public class ClientContainer
implements Externalizable, InvocationHandler
{
      
   // the "static" information that gets attached to every invocation
   public InvocationContext context;
   
   // The first interceptor in the chain
   public Interceptor next;
   
   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};
   
   public ClientContainer()
   {
      // For externalization to work
   }
   
   public ClientContainer(InvocationContext context) 
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
        
      //Create the invocation object
      Invocation invocation = new Invocation();
      
      // Contextual information for the interceptors
      invocation.setInvocationContext(context);
      
      invocation.setObjectName(context.getObjectName());
      invocation.setMethod(m);
      invocation.setArguments(args);
      
      // send the invocation down the client interceptor chain
      return next.invoke(invocation);
   }
   
   public Interceptor setNext(Interceptor interceptor) 
   {
      next = interceptor;
      
      return interceptor;
   }
   
   public void writeExternal(final ObjectOutput out)
   throws IOException
   {
      out.writeObject(next);
      out.writeObject(context);
   }

   /**
   * Externalization support.
   *
   * @param in
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
   public void readExternal(final ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      next = (Interceptor) in.readObject();
      context = (InvocationContext) in.readObject();

   }
}
 
