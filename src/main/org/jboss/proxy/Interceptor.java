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
import org.jboss.invocation.InvocationResponse;

/**
 * The base class for all interceptors.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.3 $
 *
 * <p><b>2002/2/27: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public abstract class Interceptor
   implements Externalizable
{
   /** The next interceptor in the chain. */
   protected Interceptor nextInterceptor;
 
   /**
    * Set the next interceptor in the chain.
    * 
    * <p>
    * String together the interceptors
    * We return the passed interceptor to allow for 
    * interceptor1.setNext(interceptor2).setNext(interceptor3)... constructs.
    */
   public Interceptor setNext(final Interceptor interceptor) {
      // assert interceptor != null
      nextInterceptor = interceptor;
      return interceptor;
   }
   
   public Interceptor getNext() {
      return nextInterceptor;
   }

   public abstract InvocationResponse invoke(Invocation mi) throws Throwable;
   
   /**
    * Writes the next interceptor.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(nextInterceptor);
   }

   /**
    * Reads the next interceptor.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      nextInterceptor = (Interceptor)in.readObject();
   }
}
