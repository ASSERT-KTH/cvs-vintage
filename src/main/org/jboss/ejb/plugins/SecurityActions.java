package org.jboss.ejb.plugins;

import java.security.PrivilegedAction;
import java.security.Principal;
import java.security.AccessController;

import javax.security.auth.Subject;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.RunAsIdentity;

/** A collection of privileged actions for this package
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SecurityActions
{
   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
   }
   private static class SetTCLAction implements PrivilegedAction
   {
      ClassLoader loader;
      SetTCLAction(ClassLoader loader)
      {
         this.loader = loader;
      }
      public Object run()
      {
         Thread.currentThread().setContextClassLoader(loader);
         loader = null;
         return null;
      }
   }
   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         Subject subject = SecurityAssociation.getSubject();
         return subject;
      }
   }
   private static class SetSubjectAction implements PrivilegedAction
   {
      Subject subject;
      SetSubjectAction(Subject subject)
      {
         this.subject = subject;
      }
      public Object run()
      {
         SecurityAssociation.setSubject(subject);
         return null;
      }
   }
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
   private static class PeekRunAsRoleAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new PeekRunAsRoleAction();
      public Object run()
      {
         RunAsIdentity principal = SecurityAssociation.peekRunAsIdentity();
         return principal;
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

   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(GetTCLAction.ACTION);
      return loader;
   }
   static void setContextClassLoader(ClassLoader loader)
   {
      PrivilegedAction action = new SetTCLAction(loader);
      AccessController.doPrivileged(action);
   }

   static Subject getSubject()
   {
      Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
      return subject;
   }
   static void setSubject(Subject subject)
   {
      SetSubjectAction action = new SetSubjectAction(subject);
      AccessController.doPrivileged(action);
   }
   static void setPrincipalInfo(Principal principal, Object credential)
   {
      SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential);
      AccessController.doPrivileged(action);
   }

   
   static RunAsIdentity peekRunAsIdentity()
   {
      RunAsIdentity principal = (RunAsIdentity) AccessController.doPrivileged(PeekRunAsRoleAction.ACTION);
      return principal;
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
