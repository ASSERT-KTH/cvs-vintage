/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security;

import java.security.Principal;

import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.metadata.SecurityIdentityMetaData;

/** 
 * An interceptor that enforces the run-as identity declared by a bean.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>.
 * @version $Revision: 1.2 $
 */
public final class RunAsSecurityInterceptor extends AbstractInterceptor
{
   /**
    * The security role that this container should run as.
    */
   private Principal runAsRole;

   /** 
    * We obtain the security manager and runAs identity to use here.
    */
   public void start()
   {
      SecurityIdentityMetaData securityMetaData = 
            getContainer().getBeanMetaData().getSecurityIdentityMetaData();
      if(securityMetaData != null && !securityMetaData.getUseCallerIdentity())
      {
         String roleName = securityMetaData.getRunAsRoleName();
         runAsRole = new SimplePrincipal(roleName);
      }
   }

   public void stop()
   {
      runAsRole = null;
   }

   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
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
}
