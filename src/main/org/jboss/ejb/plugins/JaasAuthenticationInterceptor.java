/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.PayloadKey;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.SecurityAssociation;

/** This interceptor is where the EJB 2.1 authentication is performed
 * along with the run-as identity establishment. 
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @version $Revision: 1.6 $
 */
public class JaasAuthenticationInterceptor extends AbstractInterceptor
{
   /** The security domain authentication service
    */
   protected AuthenticationManager securityManager;
   
   /** A static map of SecurityRolesMetaData from jboss.xml */
   protected Map securityRoles;
   /** The run-as identity for the ejb from ejb-jar.xml */
   protected RunAsIdentity runAsIdentity;

   /** Called by the super class to set the container to which this interceptor
    belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ApplicationMetaData applicationMetaData = beanMetaData.getApplicationMetaData();
         AssemblyDescriptorMetaData assemblyDescriptor = applicationMetaData.getAssemblyDescriptor();

         SecurityIdentityMetaData secMetaData = beanMetaData.getSecurityIdentityMetaData();
         if (secMetaData != null && secMetaData.getUseCallerIdentity() == false)
         {
            String roleName = secMetaData.getRunAsRoleName();
            String principalName = secMetaData.getRunAsPrincipalName();

            // the run-as principal might have extra roles mapped in the assembly-descriptor
            Set extraRoleNames = assemblyDescriptor.getSecurityRoleNamesByPrincipal(principalName);
            runAsIdentity = new RunAsIdentity(roleName, principalName, extraRoleNames);
         }

         securityManager = container.getSecurityManager();
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
      SecurityActions.pushRunAsIdentity(runAsIdentity);

      try
      {
         Object returnValue = getNext().invokeHome(mi);
         return returnValue;
      }
      finally
      {
         SecurityActions.popRunAsIdentity();
         SecurityActions.popSubjectContext();
      }
   }

   public Object invoke(Invocation mi) throws Exception
   {
      // Authenticate the subject and apply any declarative security checks
      checkSecurityAssociation(mi);

      // Save any existing caller run-as in the invocation for other interceptors
      RunAsIdentity callerRunAsIdentity = SecurityActions.peekRunAsIdentity();
      if( callerRunAsIdentity != null )
         mi.setValue("RunAsIdentity", callerRunAsIdentity, PayloadKey.TRANSIENT);

      /* If a run-as role was specified, push it so that any calls made
       by this bean will have the runAsRole available for declarative
       security checks.
      */
      SecurityActions.pushRunAsIdentity(runAsIdentity);

      try
      {
         Object returnValue = getNext().invoke(mi);
         return returnValue;
      }
      finally
      {
         SecurityActions.popRunAsIdentity();
         SecurityActions.popSubjectContext();
      }
   }

   /** Authenticate the caller using the principal and credentials in the
    * Invocation
    */
   private void checkSecurityAssociation(Invocation mi)
      throws Exception
   {
      Principal principal = mi.getPrincipal();
      Object credential = mi.getCredential();
      boolean trace = log.isInfoEnabled();

      // If there is not a security manager then there is no authentication required
      if (mi.getMethod() == null || securityManager == null || container == null)
      {
         // Allow for the progatation of caller info to other beans
         SecurityActions.pushSubjectContext(principal, credential, null);
         return;
      }

      // Authenticate the caller based on the method invocation credentials
      RunAsIdentity callerRunAsIdentity = SecurityAssociation.peekRunAsIdentity();
      if (callerRunAsIdentity == null)
      {
         /* This call associates the statically defined roles with the
         SecurityRolesAssociation thread local for use by 3.2 style of
         login modules which combined authentication and authorization.
         */
         SecurityRolesAssociation.setSecurityRoles(securityRoles);
         Subject subject = new Subject();
         if (securityManager.isValid(principal, credential, subject) == false)
         {
            // Check for the security association exception
            Exception ex = SecurityActions.getContextException();
            if( ex != null )
               throw ex;
            // Else throw a generic SecurityException
            String msg = "Authentication exception, principal=" + principal;
            SecurityException e = new SecurityException(msg);
            throw e;
         }
         else
         {
            SecurityActions.pushSubjectContext(principal, credential, subject);
            if (trace)
            {
               log.trace("Authenticated  principal=" + principal);
            }
         }
      }
   }
}
