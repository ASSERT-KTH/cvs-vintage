package org.jboss.ejb.txtimer;

import java.security.PrivilegedAction;
import java.security.AccessController;

import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;

/** A collection of privileged actions for this package
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SecurityActions
{
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

   static ClassLoader getContextClassLoader()
   {
      return TCLAction.UTIL.getContextClassLoader();
   }

   static ClassLoader getContextClassLoader(Thread thread)
   {
      return TCLAction.UTIL.getContextClassLoader(thread);
   }

   static void setContextClassLoader(ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(loader);
   }

   static void setContextClassLoader(Thread thread, ClassLoader loader)
   {
      TCLAction.UTIL.setContextClassLoader(thread, loader);
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
