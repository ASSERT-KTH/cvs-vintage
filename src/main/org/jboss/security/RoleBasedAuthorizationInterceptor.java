/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security;

import java.security.Principal;
import java.util.Set;
import javax.ejb.EJBException;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

/** 
 * The RoleBasedAuthorizationInterceptor checks that the caller principal is
 * authorized to call a method by verifing that it contains at least one
 * of the required roled.
 *
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>.
 * @version $Revision: 1.2 $
 */
public final class RoleBasedAuthorizationInterceptor extends AbstractInterceptor
{
   private AuthenticationManager securityManager;
   private RealmMapping realmMapping;

   public void start()
   {
      securityManager = getContainer().getSecurityManager();
      realmMapping = getContainer().getRealmMapping();
   }

   public void stop()
   {
      securityManager = null;
      realmMapping = null;
   }

   /**
    * Check if the principal is authorized to call the method by verifying that
    * the it containes at least one of the required roles.
    */
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      // If there is not a security manager then there is no authorization
      // required
      if(invocation.getMethod() == null || securityManager == null)
      {
         return getNext().invoke(invocation);
      }

      if(realmMapping == null)
      {
         throw new EJBException("checkSecurityAssociation", 
               new SecurityException("Role mapping manager has not been set"));
      }

      Set methodRoles = getContainer().getMethodPermissions(
            invocation.getMethod(), 
            invocation.getType());
      if(methodRoles == null)
      {
         String message = "No method permissions assigned to " +
               "method=" + invocation.getMethod().getName() +
               ", interface=" + invocation.getType();
         log.error(message);
         throw new EJBException("checkSecurityAssociation",
               new SecurityException(message));
      }

      // See if there is a runAs role associated with this thread. If there
      // is, this is the security role against which the assigned method
      // permissions must be checked.
      Principal threadRunAsRole = SecurityAssociation.peekRunAsRole();
      if(threadRunAsRole != null)
      {
         // Check the runAs role
         if(!methodRoles.contains(threadRunAsRole) &&
               !methodRoles.contains(AnybodyPrincipal.ANYBODY_PRINCIPAL))
         {
            String message = "Insufficient method permissions" +
                  ", runAsRole=" + threadRunAsRole +
                  ", method=" + invocation.getMethod().getName() + 
                  ", interface=" + invocation.getType() +
                  ", requiredRoles=" + methodRoles;

            // Dain: I think this is redundant logging
            log.error(message);
            throw new EJBException("checkSecurityAssociation", 
                  new SecurityException(message));
         }
      }
      // If the method has no assigned roles or the user does not have at
      // least one of the roles then access is denied.
      else 
      {
         Principal principal = invocation.getPrincipal();
         if(!realmMapping.doesUserHaveRole(principal, methodRoles))
         {
            String message = "Insufficient method permissions" +
                  ", principal=" + principal +
                  ", method=" + invocation.getMethod().getName() +
                  ", interface=" + invocation.getType() +
                  ", requiredRoles=" + methodRoles +
                  ", principalRoles=" + realmMapping.getUserRoles(principal);

            // Dain: I think this is redundant logging
            log.error(message);
            throw new EJBException("checkSecurityAssociation", 
                  new SecurityException(message));
         }
      }
      return getNext().invoke(invocation);
   }
}
