/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;

import java.util.Set;

/**
 * An interceptor that enforces the run-as identity declared by a bean.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @version $Revision: 1.10 $
 */
public class RunAsSecurityInterceptor extends AbstractInterceptor
{
   protected RunAsIdentity runAsIdentity;

   public RunAsSecurityInterceptor()
   {
   }

   /**
    * Called by the super class to set the container to which this interceptor
    * belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ApplicationMetaData application = beanMetaData.getApplicationMetaData();
         AssemblyDescriptorMetaData assemblyDescriptor = application.getAssemblyDescriptor();

         SecurityIdentityMetaData secMetaData = beanMetaData.getSecurityIdentityMetaData();
         if (secMetaData != null && secMetaData.getUseCallerIdentity() == false)
         {
            String roleName = secMetaData.getRunAsRoleName();
            String principalName = secMetaData.getRunAsPrincipalName();
            // the run-as principal might have extra roles mapped in the assembly-descriptor
            Set extraRoleNames = assemblyDescriptor.getSecurityRoleNamesByPrincipal(principalName);
            runAsIdentity = new RunAsIdentity(roleName, principalName, extraRoleNames);
         }
      }
   }

   // Container implementation --------------------------------------
   public void start() throws Exception
   {
      super.start();
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
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

}
