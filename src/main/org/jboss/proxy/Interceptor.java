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
import org.jboss.invocation.Invocation;

/*
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
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
import org.jboss.invocation.Invoker;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.security.SecurityAssociation;
*/
/**
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1 $
 *
 * <p><b>2002/2/27: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public abstract class Interceptor
   implements Externalizable
{
   
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The next interceptor in the chain. */
   protected Interceptor nextInterceptor;
 
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation ------------------------------------
  
   /**
    * setNext()
    * 
    * String together the interceptors
    * We return the passed interceptor to allow for 
    * interceptor1.setNext(interceptor2).setNext(interceptor3)... constructs.
    * 
    */
   public Interceptor setNext(final Interceptor interceptor) {
      // assert interceptor != null
      nextInterceptor = interceptor;
      return interceptor;
   }
   
   public Interceptor getNext() {
      
      return nextInterceptor;
   }

   public abstract Object invoke(Invocation mi) throws Throwable;
   
   /**
   * Externalization support.
   *
   * @param out
   *
   * @throws IOException
   */
   public void writeExternal(final ObjectOutput out)
   throws IOException
   {
      out.writeObject(nextInterceptor);
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
      nextInterceptor = (Interceptor) in.readObject();
   }
   // Protected -----------------------------------------------------
}