/*
 * JBoss, the OpenSource webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.connector.invoker;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;

import org.jboss.security.SecurityAssociation;

/** Common PrivilegedAction used by classes in this package.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
class SecurityActions
{
   private static class GetSubjectAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetSubjectAction();
      public Object run()
      {
         Subject subject = SecurityAssociation.getSubject();
         return subject;
      }
   }
   private static class GetPrincipalAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetPrincipalAction();
      public Object run()
      {
         Principal principal = SecurityAssociation.getPrincipal();
         return principal;
      }
   }
   private static class GetCredentialAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetCredentialAction();
      public Object run()
      {
         Object credential = SecurityAssociation.getCredential();
         return credential;
      }
   }
   private static class SetPrincipalInfoAction implements PrivilegedAction
   {
      Principal principal;
      Object credential;
      Subject subject;
      boolean setSubject;
      SetPrincipalInfoAction(Principal principal, Object credential)
      {
         this.principal = principal;
         this.credential = credential;
      }
      SetPrincipalInfoAction(Principal principal, Object credential, Subject subject)
      {
         this.principal = principal;
         this.credential = credential;
         this.subject = subject;
         this.setSubject = true;
      }

      public Object run()
      {
         SecurityAssociation.setCredential(credential);
         credential = null;
         SecurityAssociation.setPrincipal(principal);
         principal = null;
         if( setSubject == true )
            SecurityAssociation.setSubject(subject);
         subject = null;
         return null;
      }
   }
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

   static Principal getPrincipal()
   {
      Principal principal = (Principal) AccessController.doPrivileged(GetPrincipalAction.ACTION);
      return principal;
   }
   static Object getCredential()
   {
      Object credential = AccessController.doPrivileged(GetCredentialAction.ACTION);
      return credential;
   }
   static Subject getActiveSubject()
   {
      Subject subject = (Subject) AccessController.doPrivileged(GetSubjectAction.ACTION);
      return subject;
   }
   static void setPrincipalInfo(Principal principal, Object credential, Subject subject)
   {
      SetPrincipalInfoAction action = new SetPrincipalInfoAction(principal, credential, subject);
      AccessController.doPrivileged(action);
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
}
