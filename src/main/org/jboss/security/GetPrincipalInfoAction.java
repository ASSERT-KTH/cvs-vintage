package org.jboss.security;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Principal;

import org.jboss.security.SecurityAssociation;

/** A PrivilegedAction implementation for accessing the SecurityAssociation
 * principal and credentials.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
class GetPrincipalInfoAction
{
   private static class GetPrincipalAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetPrincipalAction();
      public Object run()
      {
         Principal principal = SecurityAssociation.getPrincipal();
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
   }

   static Principal getPrincipal()
   {
      Principal principal = (Principal) AccessController.doPrivileged(GetPrincipalAction.ACTION);
      return principal;
   }
   static Object getCredential()
   {
      Object credential = AccessController.doPrivileged(GetCredentialAction.ACTION);
      return credential;
   }

}
