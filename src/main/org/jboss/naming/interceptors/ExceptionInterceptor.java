/*
* JBoss, the OpenSource J2EE WebOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.naming.interceptors;

import java.io.Externalizable;
import java.io.IOException;

import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.proxy.Interceptor;

/** A client interceptor that handles the wrapping of exceptions to
 * NamingExceptions
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.2 $
 */
public class ExceptionInterceptor extends Interceptor
   implements Externalizable
{
   /** The serialVersionUID. @since 1.2 */
   private static final long serialVersionUID = 9208056528786546687L;

   /** Handle methods locally on the client
    *
    * @param mi
    * @return
    * @throws Throwable
    */
   public InvocationResponse invoke(Invocation mi) throws Throwable
   {
      Object value = null;
      try
      {
         value = getNext().invoke(mi);
      }
      catch(NamingException e)
      {
         throw e;
      }
      catch(IOException e)
      {
         CommunicationException ce = new CommunicationException("Operation failed");
         ce.setRootCause(e);
         throw ce;
      }
      catch(Throwable t)
      {
         ServiceUnavailableException sue = new ServiceUnavailableException("Unexpected failure");
         sue.setRootCause(t);
         throw sue;
      }

      return new InvocationResponse(value);
   }

}
