package org.jboss.ejb.plugins;

import java.security.PrivilegedAction;
import java.security.Principal;
import java.security.AccessController;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.RunAsIdentity;

/** A collection of privileged actions for this package
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SecurityActions
{
   private static class SetPrincipalInfoAction implements PrivilegedAction
   {
      Principal principal;
      Object credential;
      SetPrincipalInfoAction(Principal principal, Object credential)
      {
         this.principal = principal;
         this.credential = credential;
      }
      public Object run()
      {
         SecurityAssociation.setCredential(credential);
         credential = null;
         SecurityAssociation.setPrincipal(principal);
         principal = null;
         return null;
      }
   }
   private static class PushRunAsRoleAction implements PrivilegedAction
   {
      RunAsIdentity principal;
      PushRunAsRoleAction(RunAsIdentity principal)
      {
         this.principal = principal;
      }
      public Object run()
      {
         SecurityAssociation.pushRunAsIdentity(principal);
         return null;
      }
   }

   private static class PopRunAsRoleAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new PopRunAsRoleAction();
      public Object run()
      {
         RunAsIdentity principal = SecurityAssociation.popRunAsIdentity();
         return principal;
      }
   }

   static void setPrincipalInfo(Principal principal, Object credential)
   {
      SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential);
      AccessController.doPrivileged(action);
   }

   static void pushRunAsIdentity(RunAsIdentity principal)
   {
      PushRunAsRoleAction action = new PushRunAsRoleAction(principal);
      AccessController.doPrivileged(action);
   }
   static RunAsIdentity popRunAsIdentity()
   {
      RunAsIdentity principal = (RunAsIdentity) AccessController.doPrivileged(PopRunAsRoleAction.ACTION);
      return principal;
   }
}
