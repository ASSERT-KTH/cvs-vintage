/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Set;
import javax.ejb.EJBException;
import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyContext;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.BeanMetaData;

/** This interceptor is where the JACC ejb container authorization is performed.
 *
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 * @version $Revision: 1.1 $
 */
public class JaccAuthorizationInterceptor extends AbstractInterceptor
{
   /** The JACC PolicyContext key for the current Subject */
   private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";
   private Policy policy;
   private String ejbName;
   private CodeSource ejbCS;

   /** Called by the super class to set the container to which this interceptor
    belongs. We obtain the security manager and runAs identity to use here.
    */
   public void setContainer(Container container)
   {
      super.setContainer(container);
      if (container != null)
      {
         BeanMetaData beanMetaData = container.getBeanMetaData();
         ejbName = beanMetaData.getEjbName();
         ejbCS = container.getBeanClass().getProtectionDomain().getCodeSource();
      }
      policy = Policy.getPolicy();
   }

   // Container implementation --------------------------------------
   public void start() throws Exception
   {
      super.start();
   }

   public Object invokeHome(Invocation mi) throws Exception
   {
      // Authorize the call
      checkSecurityAssociation(mi);
      Object returnValue = getNext().invokeHome(mi);
      return returnValue;
   }

   public Object invoke(Invocation mi) throws Exception
   {
      // Authorize the call
      checkSecurityAssociation(mi);
      Object returnValue = getNext().invoke(mi);
      return returnValue;
   }

   /** Authorize the caller's access to the method invocation
    */
   private void checkSecurityAssociation(Invocation mi)
      throws Exception
   {
      Method m = mi.getMethod();
      String iface = mi.getType().toInterfaceString();
      EJBMethodPermission methodPerm = new EJBMethodPermission(ejbName, iface, m);
      // Get the caller, return if there is no authenticated caller
      Subject caller = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
      if( caller == null )
         return;

      // Get the caller principals
      Set principalsSet = caller.getPrincipals();
      Principal[] principals = new Principal[principalsSet.size()];
      principalsSet.toArray(principals);      

      ProtectionDomain pd = new ProtectionDomain (ejbCS, null, null, principals);
      if( policy.implies(pd, methodPerm) == false )
      {
         String msg = "Denied: "+methodPerm+", caller=" + caller;
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new EJBException("checkSecurityAssociation", e);
      }
   }
}
