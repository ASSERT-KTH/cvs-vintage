/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.security;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.Principal;
import java.security.AccessController;
import java.security.PrivilegedAction;

/** An implementation of Authenticator that obtains the username and password
 * from the current SecurityAssociation state.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.5 $
 */
public class SecurityAssociationAuthenticator extends Authenticator
{
   protected PasswordAuthentication getPasswordAuthentication()
   {
      SecurityActions sa = SecurityActions.UTIL.getSecurityActions();
      Principal principal = sa.getPrincipal();
      Object credential = sa.getCredential();
      String name = principal != null ? principal.getName() : null;
      char[] password = {};
      if( credential != null )
      {
         if( password.getClass().isInstance(credential) )
            password = (char[]) credential;
         else
            password = credential.toString().toCharArray();
      }
      PasswordAuthentication auth = new PasswordAuthentication(name, password);
      return auth;
   }

   interface SecurityActions
   {
      class UTIL
      {
         static SecurityActions getSecurityActions()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }
      }

      SecurityActions NON_PRIVILEGED = new SecurityActions()
      {
         public Principal getPrincipal()
         {
            return SecurityAssociation.getPrincipal();
         }

         public Object getCredential()
         {
            return SecurityAssociation.getCredential();
         }
      };

      SecurityActions PRIVILEGED = new SecurityActions()
      {
         private final PrivilegedAction getPrincipalAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getPrincipal();
            }
         };

         private final PrivilegedAction getCredentialAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getCredential();
            }
         };

         public Principal getPrincipal()
         {
            return (Principal)AccessController.doPrivileged(getPrincipalAction);
         }

         public Object getCredential()
         {
            return AccessController.doPrivileged(getCredentialAction);
         }
      };

      Principal getPrincipal();

      Object getCredential();
   }
}
