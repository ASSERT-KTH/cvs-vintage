/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.security.Principal;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/** 
 * An interceptor that enforces the run-as identity declared by a bean.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @version $Revision: 1.3 $
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
   public void create(Container container)
   {
      SecurityIdentityMetaData securityMetaData = 
            getContainer().getBeanMetaData().getSecurityIdentityMetaData();
      if(securityMetaData != null && !securityMetaData.getUseCallerIdentity())
      {
         String roleName = securityMetaData.getRunAsRoleName();
         runAsRole = new SimplePrincipal(roleName);
      }
   }

   public void destory()
   {
      runAsRole = null;
   }

   public Object invoke(Invocation invocation) throws Exception
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
