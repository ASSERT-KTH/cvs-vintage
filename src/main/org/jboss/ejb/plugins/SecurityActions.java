package org.jboss.ejb.plugins;

import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.Principal;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.lang.reflect.UndeclaredThrowableException;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.RunAsIdentity;

/** A collection of privileged actions for this package
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revison: $
 */
class SecurityActions
{
   interface PrincipalInfoAction
   {
      PrincipalInfoAction PRIVILEGED = new PrincipalInfoAction()
      {
         public void push(final Principal principal, final Object credential,
            final Subject subject)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     SecurityAssociation.pushSubjectContext(subject, principal, credential);
                     return null;
                  }
               }
            );
         }
         public void pop()
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     SecurityAssociation.popSubjectContext();
                     return null;
                  }
               }
            );
         }
      };

      PrincipalInfoAction NON_PRIVILEGED = new PrincipalInfoAction()
      {
         public void push(Principal principal, Object credential, Subject subject)
         {
            SecurityAssociation.pushSubjectContext(subject, principal, credential);
         }
         public void pop()
         {
            SecurityAssociation.popSubjectContext();
         }
      };

      void push(Principal principal, Object credential, Subject subject);
      void pop();
   }

   interface RunAsIdentityActions
   {
      RunAsIdentityActions PRIVILEGED = new RunAsIdentityActions()
      {
         private final PrivilegedAction peekAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.peekRunAsIdentity();
            }
         };

         private final PrivilegedAction popAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.popRunAsIdentity();
            }
         };

         public RunAsIdentity peek()
         {
            return (RunAsIdentity)AccessController.doPrivileged(peekAction);
         }

         public void push(final RunAsIdentity id)
         {
            AccessController.doPrivileged(
               new PrivilegedAction()
               {
                  public Object run()
                  {
                     SecurityAssociation.pushRunAsIdentity(id);
                     return null;
                  }
               }
            );
         }

         public RunAsIdentity pop()
         {
            return (RunAsIdentity)AccessController.doPrivileged(popAction);
         }
      };

      RunAsIdentityActions NON_PRIVILEGED = new RunAsIdentityActions()
      {
         public RunAsIdentity peek()
         {
            return SecurityAssociation.peekRunAsIdentity();
         }

         public void push(RunAsIdentity id)
         {
            SecurityAssociation.pushRunAsIdentity(id);
         }

         public RunAsIdentity pop()
         {
            return SecurityAssociation.popRunAsIdentity();
         }
      };

      RunAsIdentity peek();

      void push(RunAsIdentity id);

      RunAsIdentity pop();
   }

   interface ContextInfoActions
   {
      static final String EX_KEY = "org.jboss.security.exception";
      ContextInfoActions PRIVILEGED = new ContextInfoActions()
      {
         private final PrivilegedAction exAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getContextInfo(EX_KEY);
            }
         };
         public Exception getContextException()
         {
            return (Exception)AccessController.doPrivileged(exAction);
         }
      };

      ContextInfoActions NON_PRIVILEGED = new ContextInfoActions()
      {
         public Exception getContextException()
         {
            return (Exception)SecurityAssociation.getContextInfo(EX_KEY);
         }
      };

      Exception getContextException();
   }

   interface PolicyContextActions
   {
      /** The JACC PolicyContext key for the current Subject */
      static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";
      PolicyContextActions PRIVILEGED = new PolicyContextActions()
      {
         private final PrivilegedExceptionAction exAction = new PrivilegedExceptionAction()
         {
            public Object run() throws Exception
            {
               return (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
            }
         };
         public Subject getContextSubject()
            throws PolicyContextException
         {
            try
            {
               return (Subject) AccessController.doPrivileged(exAction);
            }
            catch(PrivilegedActionException e)
            {
               Exception ex = e.getException();
               if( ex instanceof PolicyContextException )
                  throw (PolicyContextException) ex;
               else
                  throw new UndeclaredThrowableException(ex);
            }
         }
      };

      PolicyContextActions NON_PRIVILEGED = new PolicyContextActions()
      {
         public Subject getContextSubject()
            throws PolicyContextException
         {
            return (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
         }
      };

      Subject getContextSubject()
         throws PolicyContextException;
   }
   
   static ClassLoader getContextClassLoader()
   {
      return TCLAction.UTIL.getContextClassLoader();
   }

   static void setContextClassLoader(ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(loader);
   }

   static void pushSubjectContext(Principal principal, Object credential,
      Subject subject)
   {
      if(System.getSecurityManager() == null)
      {
         PrincipalInfoAction.NON_PRIVILEGED.push(principal, credential, subject);
      }
      else
      {
         PrincipalInfoAction.PRIVILEGED.push(principal, credential, subject);
      }
   }
   static void popSubjectContext()
   {
      if(System.getSecurityManager() == null)
      {
         PrincipalInfoAction.NON_PRIVILEGED.pop();
      }
      else
      {
         PrincipalInfoAction.PRIVILEGED.pop();
      }
   }

   static RunAsIdentity peekRunAsIdentity()
   {
      if(System.getSecurityManager() == null)
      {
         return RunAsIdentityActions.NON_PRIVILEGED.peek();
      }
      else
      {
         return RunAsIdentityActions.PRIVILEGED.peek();
      }
   }

   static void pushRunAsIdentity(RunAsIdentity principal)
   {
      if(System.getSecurityManager() == null)
      {
         RunAsIdentityActions.NON_PRIVILEGED.push(principal);
      }
      else
      {
         RunAsIdentityActions.PRIVILEGED.push(principal);
      }
   }

   static RunAsIdentity popRunAsIdentity()
   {
      if(System.getSecurityManager() == null)
      {
         return RunAsIdentityActions.NON_PRIVILEGED.pop();
      }
      else
      {
         return RunAsIdentityActions.PRIVILEGED.pop();
      }
   }

   static Exception getContextException()
   {
      if(System.getSecurityManager() == null)
      {
         return ContextInfoActions.NON_PRIVILEGED.getContextException();
      }
      else
      {
         return ContextInfoActions.PRIVILEGED.getContextException();
      }
   }

   static Subject getContextSubject()
      throws PolicyContextException
   {
      if(System.getSecurityManager() == null)
      {
         return PolicyContextActions.NON_PRIVILEGED.getContextSubject();
      }
      else
      {
         return PolicyContextActions.PRIVILEGED.getContextSubject();
      }      
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
