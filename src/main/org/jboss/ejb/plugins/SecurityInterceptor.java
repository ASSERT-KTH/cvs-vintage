/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/** 
 * The SecurityInterceptor is where the EJB 2.0 declarative security model
 * is enforced. This is where the caller identity propagation is controlled 
 * as well.
 *
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @version $Revision: 1.33 $
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

   /**
    * The security role that this container should run as.
    */
   protected Principal runAsRole;

   /** 
    * Called by the super class to set the container to which this
    * interceptor belongs. We obtain the security manager and runAs 
    * identity to use here.
    */
   public void start()
   {
      securityManager = getContainer().getSecurityManager();
      realmMapping = getContainer().getRealmMapping();

      // get the run as role
      SecurityIdentityMetaData securityMetaData = 
            getContainer().getBeanMetaData().getSecurityIdentityMetaData();
      if(securityMetaData != null && !securityMetaData.getUseCallerIdentity())
      {
         String roleName = securityMetaData.getRunAsRoleName();
         runAsRole = new SimplePrincipal(roleName);
      }
   }

   public Object invoke(Invocation invocation) throws Exception
   {
      // Authenticate the subject and apply any declarative security checks
      checkSecurityAssociation(invocation);

      // If a run-as role was specified, push it so that any calls made
      // by this bean will have the runAsRole available for declarative
      // security checks.
      if(runAsRole != null)
      {
         SecurityAssociation.pushRunAsRole(runAsRole);
      }

      try
      {
         return getNext().invoke(invocation);
      }
      finally
      {
         if(runAsRole != null)
         {
            SecurityAssociation.popRunAsRole();
         }
      }
   }

   /**
    * The EJB 2.0 declarative security algorithm:
    * 1. Authenticate the caller using the principal and credentials in the 
    * MethodInfocation
    * 2. Validate access to the method by checking the principal's roles 
    * against those required to access the method.
    */
   private void checkSecurityAssociation(Invocation invocation) throws Exception
   {
      Principal principal = invocation.getPrincipal();
      Object credential = invocation.getCredential();

      // If there is not a security manager then there is no authentication
      // required
      if(invocation.getMethod() == null || securityManager == null)
      {
         // Allow for the progatation of caller info to other beans
         SecurityAssociation.setPrincipal( principal );
         SecurityAssociation.setCredential( credential );
         return;
      }

      if(realmMapping == null)
      {
         throw new EJBException("checkSecurityAssociation", 
               new SecurityException("Role mapping manager has not been set"));
      }

      // Check the security info from the method invocation
      if(!securityManager.isValid(principal, credential))
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
      }

      InvocationType iface = invocation.getType();
      Set methodRoles = getContainer().getMethodPermissions(
            invocation.getMethod(), 
            iface);
      if(methodRoles == null)
      {
         String method = invocation.getMethod().getName();
         String msg = "No method permissions assigned to method=" + method +
               ", interface=" + iface;
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new EJBException("checkSecurityAssociation", e);
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
            String method = invocation.getMethod().getName();
            String msg = "Insufficient method permissions" +
                  ", runAsRole=" + threadRunAsRole +
                  ", method=" + method + 
                  ", interface="+iface +
                  ", requiredRoles=" + methodRoles;
            // Dain: I think this is redundant logging
            log.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new EJBException("checkSecurityAssociation", e);
         }
      }

      // If the method has no assigned roles or the user does not have at
      // least one of the roles then access is denied.
      else if(!realmMapping.doesUserHaveRole(principal, methodRoles))
      {
         String method = invocation.getMethod().getName();
         Set userRoles = realmMapping.getUserRoles(principal);
         String msg = "Insufficient method permissions" +
            ", principal=" + principal +
            ", method=" + method +
            ", interface=" + iface +
            ", requiredRoles=" + methodRoles +
            ", principalRoles="+userRoles;
         // Dain: I think this is redundant logging
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new EJBException("checkSecurityAssociation", e);
      }
   }
}
