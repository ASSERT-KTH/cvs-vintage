/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy;

import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.AccessController;

import org.jboss.invocation.Invocation;
import org.jboss.security.SecurityAssociation;

/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.9 $
*/
public class SecurityInterceptor
   extends Interceptor
{
   /** Serial Version Identifier. @since 1.4.2.1 */
   private static final long serialVersionUID = -4206940878404525061L;

   /**
   * No-argument constructor for externalization.
   */
   public SecurityInterceptor()
   {
   }

   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      // Get Principal and credentials 
      Principal principal = GetPrincipalAction.getPrincipal();
      if (principal != null)
      {
         invocation.setPrincipal(principal);
      }

      Object credential = GetCredentialAction.getCredential();
      if (credential != null)
      {
         invocation.setCredential(credential);
      }

      return getNext().invoke(invocation);
   }

   private static class GetPrincipalAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetPrincipalAction();
      public Object run()
      {
         Principal principal = SecurityAssociation.getPrincipal();
         return principal;
      }
      static Principal getPrincipal()
      {
         Principal principal = (Principal) AccessController.doPrivileged(ACTION);
         return principal;
      }
   }

   private static class GetCredentialAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetCredentialAction();
      public Object run()
      {
         Object credential = SecurityAssociation.getCredential();
         return credential;
      }
      static Object getCredential()
      {
         Object credential = AccessController.doPrivileged(ACTION);
         return credential;
      }
   }
}
