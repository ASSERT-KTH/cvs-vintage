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
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.AssemblyDescriptorMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;

/** The declarative roles based authorization interceptor which uses the
 * RealmMapping interface of the associated security domain.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @author <a href="mailto:Thomas.Diesler@jboss.org">Thomas Diesler</a>.
 * @version $Revision: 1.1 $
 */
public class SecurityRolesInterceptor extends AbstractInterceptor
{
   /** The security domain authorization service */
   protected RealmMapping realmMapping;

   /** A static map of SecurityRolesMetaData from jboss.xml */
   protected Map securityRoles;

   /** Called by the super class to set the container to which this interceptor
    belongs. We obtain the authorization service here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ApplicationMetaData applicationMetaData = beanMetaData.getApplicationMetaData();
         AssemblyDescriptorMetaData assemblyDescriptor = applicationMetaData.getAssemblyDescriptor();
         securityRoles = assemblyDescriptor.getSecurityRoles();

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
      // Apply any declarative security checks
      checkSecurityAssociation(mi);
      Object returnValue = getNext().invokeHome(mi);
      return returnValue;
   }

   public Object invoke(Invocation mi) throws Exception
   {
      // Authenticate the subject and apply any declarative security checks
      checkSecurityAssociation(mi);
      Object returnValue = getNext().invoke(mi);
      return returnValue;
   }

   /** Validate access to the method by checking the principal's roles against
    those required to access the method.
    @param mi the method invocation context
    */
   private void checkSecurityAssociation(Invocation mi)
      throws Exception
   {
      Principal principal = mi.getPrincipal();
      boolean trace = log.isInfoEnabled();

      if (realmMapping == null)
      {
         throw new EJBException("checkSecurityAssociation",
            new SecurityException("Role mapping manager has not been set"));
      }

      // Get the method permissions
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

      // Check if the caller is allowed to access the method
      RunAsIdentity callerRunAsIdentity = SecurityAssociation.peekRunAsIdentity();
      if (methodRoles.contains(AnybodyPrincipal.ANYBODY_PRINCIPAL) == false)
      {
         // The caller is using a the caller identity
         if (callerRunAsIdentity == null)
         {
            // Now actually check if the current caller has one of the required method roles
            if (realmMapping.doesUserHaveRole(principal, methodRoles) == false)
            {
               Set userRoles = realmMapping.getUserRoles(principal);
               String method = mi.getMethod().getName();
               String msg = "Insufficient method permissions, principal=" + principal
                  + ", method=" + method + ", interface=" + iface
                  + ", requiredRoles=" + methodRoles + ", principalRoles=" + userRoles;
               log.error(msg);
               SecurityException e = new SecurityException(msg);
               throw new EJBException("checkSecurityAssociation", e);
            }
         }

         // The caller is using a run-as identity
         else
         {
            // Check that the run-as role is in the set of method roles
            if (callerRunAsIdentity.doesUserHaveRole(methodRoles) == false)
            {
               String method = mi.getMethod().getName();
               String msg = "Insufficient method permissions, runAsPrincipal=" + callerRunAsIdentity.getName()
                  + ", method=" + method + ", interface=" + iface
                  + ", requiredRoles=" + methodRoles + ", runAsRoles=" + callerRunAsIdentity.getRunAsRoles();
               log.error(msg);
               SecurityException e = new SecurityException(msg);
               throw new EJBException("checkSecurityAssociation", e);
            }
         }
      }
   }
}
