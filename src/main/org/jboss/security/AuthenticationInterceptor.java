/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security;

import java.security.Principal;
import javax.ejb.EJBException;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

/** 
 * The AuthenticationInterceptor authenticates the caller.
 *
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>.
 * @version $Revision: 1.2 $
 */
public final class AuthenticationInterceptor extends AbstractInterceptor
{
   private AuthenticationManager authenticationManager;

   public void start()
   {
      authenticationManager = getContainer().getSecurityManager();
   }

   public void stop()
   {
      authenticationManager = null;
   }

   /**
    * Authenticates the caller using the principal and credentials in the 
    * Infocation if thre is a security manager and an invcocation method.
    */
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      Principal principal = invocation.getPrincipal();
      Object credential = invocation.getCredential();

      // Check the security info from the method invocation if there is a 
      // security manager and an invocation method.
      if(invocation.getMethod() != null && authenticationManager != null &&
            !authenticationManager.isValid(principal, credential))
      {
         // Authentication has failed
         String message = "Authentication exception, principal=" + principal;
         log.error(message);
         SecurityException e = new SecurityException(message);
         throw new EJBException("checkSecurityAssociation", e);
      }
      else
      {
         // Associate principal and credential with the thread for other beans
         SecurityAssociation.setPrincipal(principal);
         SecurityAssociation.setCredential(credential);
      }

      return getNext().invoke(invocation);
   }
}
