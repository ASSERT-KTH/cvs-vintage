/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.security.Principal;
import java.util.Set;
import java.util.HashSet;
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.*;

/**
 * The SecurityInterceptor is where the EJB 2.0 declarative security model
 * is enforced. This is where the caller identity propagation is controlled as well.
 *
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @version $Revision: 1.38 $
 */
public class SecurityInterceptor extends AbstractInterceptor
{
   /**
    * @supplierCardinality 0..1
    * @supplierQualifier authentication
    * @clientCardinality 1..*
    */
   protected AuthenticationManager securityManager;

   /**
    * @supplierCardinality 0..1
    * @clientCardinality 1..*
    * @supplierQualifier identity mapping
    */
   protected RealmMapping realmMapping;
   protected RunAsIdentity runAsIdentity;

   public SecurityInterceptor()
   {
   }

   /** Called by the super class to set the container to which this interceptor
    belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         SecurityIdentityMetaData secMetaData = beanMetaData.getSecurityIdentityMetaData();
         if (secMetaData != null && secMetaData.getUseCallerIdentity() == false)
         {
            String roleName = secMetaData.getRunAsRoleName();
            String principalName = secMetaData.getRunAsPrincipalName();
            String credential = secMetaData.getRunAsCredential();
            runAsIdentity = new RunAsIdentity(roleName, principalName, credential);
         }
         securityManager = container.getSecurityManager();
         realmMapping = container.getRealmMapping();
      }
   }

   // Container implementation --------------------------------------
   public void start() throws Exception
   {
      super.start();
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
      // Authenticate the subject and apply any declarative security checks
      checkSecurityAssociation(mi);

      /* If a run-as role was specified, push it so that any calls made
       by this bean will have the runAsRole available for declarative
       security checks.
      */
      if (runAsIdentity != null)
      {
         SecurityAssociation.pushRunAsIdentity(runAsIdentity);
      }

      try
      {
         Object returnValue = getNext().invokeHome(mi);
         return returnValue;
      }
      finally
      {
         if (runAsIdentity != null)
         {
            SecurityAssociation.popRunAsIdentity();
         }
      }
   }

   public Object invoke(Invocation mi) throws Exception
   {
      // Authenticate the subject and apply any declarative security checks
      checkSecurityAssociation(mi);

      /* If a run-as role was specified, push it so that any calls made
       by this bean will have the runAsRole available for declarative
       security checks.
      */
      if (runAsIdentity != null)
      {
         SecurityAssociation.pushRunAsIdentity(runAsIdentity);
      }

      try
      {
         Object returnValue = getNext().invoke(mi);
         return returnValue;
      }
      finally
      {
         if (runAsIdentity != null)
         {
            SecurityAssociation.popRunAsIdentity();
         }
      }
   }

   /** The EJB 2.0 declarative security algorithm:
    1. Authenticate the caller using the principal and credentials in the MethodInfocation
    2. Validate access to the method by checking the principal's roles against
    those required to access the method.
    */
   private void checkSecurityAssociation(Invocation mi)
      throws Exception
   {
      Principal principal = mi.getPrincipal();
      Object credential = mi.getCredential();
      boolean trace = log.isInfoEnabled();

      /* If there is not a security manager then there is no authentication
       required
       */
      if (mi.getMethod() == null || securityManager == null)
      {
         // Allow for the progatation of caller info to other beans
         SecurityAssociation.setPrincipal(principal);
         SecurityAssociation.setCredential(credential);
         return;
      }

      if (realmMapping == null)
      {
         throw new EJBException("checkSecurityAssociation",
            new SecurityException("Role mapping manager has not been set"));
      }

      // authenticate the current principal
      RunAsIdentity callerRunAsIdentity = SecurityAssociation.peekRunAsIdentity();
      boolean isAnonymousRunAsPrincipal = callerRunAsIdentity != null && callerRunAsIdentity.isAnonymousPrincipal();
      if (isAnonymousRunAsPrincipal == false)
      {
         // Check the security info from the method invocation
         if (securityManager.isValid(principal, credential) == false)
         {
            String msg = "Authentication exception, principal=" + principal;
            log.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new EJBException("checkSecurityAssociation", e);
         }
         else
         {
            SecurityAssociation.setPrincipal(principal);
            SecurityAssociation.setCredential(credential);
            if (trace)
            {
               log.trace("Authenticated  principal=" + principal);
            }
         }
      }

      InvocationType iface = mi.getType();
      Set methodRoles = container.getMethodPermissions(mi.getMethod(), iface);
      if (methodRoles == null)
      {
         String method = mi.getMethod().getName();
         String msg = "No method permissions assigned to method=" + method
            + ", interface=" + iface;
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new EJBException("checkSecurityAssociation", e);
      }
      else if (trace)
      {
         log.trace("method=" + mi.getMethod() + ", interface=" + iface
            + ", requiredRoles=" + methodRoles);
      }

      // If the current caller is an anonymous run-as principal, we don't go to the JAASSecurityManager
      RealmMapping localRealmMapping = (isAnonymousRunAsPrincipal ? new AnonymousRunAsRealmMapping() : realmMapping);

      // Get the current caller's user roles
      Set userRoles = localRealmMapping.getUserRoles(principal);

      // The caller is using a run-as identity
      if (callerRunAsIdentity != null)
      {
         // first check that the current run-as principal actually has the run-as role that
         // he claims to have in the deployment descriptor
         Principal runAsRole = callerRunAsIdentity.getRunAsRole();
         if (userRoles.contains(runAsRole) == false)
         {
            String msg = "Insufficient role permissions, runAs=" + callerRunAsIdentity
               + ", currentRoles=" + userRoles;
            log.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new EJBException("checkSecurityAssociation", e);
         }

         /*
         // Note, this check causes a CTS failure
         // http://tck1.jboss.com/jira/secure/ViewIssue.jspa?id=10106

         // Check that the run-as role is in the set of method roles
         if (methodRoles.contains(runAsRole) == false && methodRoles.contains(AnybodyPrincipal.ANYBODY_PRINCIPAL) == false)
         {
            String method = mi.getMethod().getName();
            String msg = "Insufficient method permissions, runAs=" + callerRunAsIdentity
               + ", method=" + method + ", interface=" + iface
               + ", requiredRoles=" + methodRoles + ", principalRoles=" + userRoles;
            log.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new EJBException("checkSecurityAssociation", e);
         }
         */
      }

      // Now actually check if the current caller has one of the required method roles
      if (localRealmMapping.doesUserHaveRole(principal, methodRoles) == false)
      {
         String method = mi.getMethod().getName();
         String msg = "Insufficient method permissions, principal=" + principal
            + ", method=" + method + ", interface=" + iface
            + ", requiredRoles=" + methodRoles + ", principalRoles=" + userRoles;
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new EJBException("checkSecurityAssociation", e);
      }
   }

   /**
    * Implements the realm mapping for the anonymous run-as principal.
    */
   public static class AnonymousRunAsRealmMapping implements RealmMapping
   {

      /** Return the given principal */
      public Principal getPrincipal(Principal principal)
      {
         return principal;
      }

      /** True if the given roles contain the run-as role */
      public boolean doesUserHaveRole(Principal principal, Set roles)
      {
         RunAsIdentity runAs = (RunAsIdentity)principal;
         return roles.contains(runAs.getRunAsRole());
      }

      /** Return a set that contains the single run-as role */
      public Set getUserRoles(Principal principal)
      {
         RunAsIdentity runAs = (RunAsIdentity)principal;
         Set roles = new HashSet();
         roles.add(runAs.getRunAsRole());
         return roles;
      }
   }
}
