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
      return TCLAction.UTIL.getContextClassLoader();
   }
   static void setContextClassLoader(ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(loader);
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

   interface TCLAction
   {
      class UTIL
      {
         static TCLAction getTCLAction()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }

         static ClassLoader getContextClassLoader()
         {
            return getTCLAction().getContextClassLoader();
         }

         static ClassLoader getContextClassLoader(Thread thread)
         {
            return getTCLAction().getContextClassLoader(thread);
         }

         static void setContextClassLoader(ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(cl);
         }

         static void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            getTCLAction().setContextClassLoader(thread, cl);
         }
      }

      TCLAction NON_PRIVILEGED = new TCLAction()
      {
         public ClassLoader getContextClassLoader()
         {
            return Thread.currentThread().getContextClassLoader();
         }

         public ClassLoader getContextClassLoader(Thread thread)
         {
            return thread.getContextClassLoader();
         }

         public void setContextClassLoader(ClassLoader cl)
         {
            Thread.currentThread().setContextClassLoader(cl);
         }

         public void setContextClassLoader(Thread thread, ClassLoader cl)
         {
            thread.setContextClassLoader(cl);
         }
      };

      TCLAction PRIVILEGED = new TCLAction()
      {
         private final PrivilegedAction getTCLPrivilegedAction = new PrivilegedAction()
         {
            public Object run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         };

         public ClassLoader getContextClassLoader()
         {
            return (ClassLoader)AccessController.doPrivileged(getTCLPrivilegedAction);
         }

         public ClassLoader getContextClassLoader(final Thread thread)
         {
            return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  return thread.getContextClassLoader();
               }
            });
         }

         public void setContextClassLoader(final ClassLoader cl)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     Thread.currentThread().setContextClassLoader(cl);
                     return null;
                  }
               }
            );
         }

         public void setContextClassLoader(final Thread thread, final ClassLoader cl)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     thread.setContextClassLoader(cl);
                     return null;
                  }
               }
            );
         }
      };

      ClassLoader getContextClassLoader();

      ClassLoader getContextClassLoader(Thread thread);

      void setContextClassLoader(ClassLoader cl);

      void setContextClassLoader(Thread thread, ClassLoader cl);
   }
}
