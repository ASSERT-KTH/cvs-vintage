/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.security.PrivilegedAction;
import java.security.AccessController;

/**
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version <tt>$Revision: 1.2 $</tt>
 */
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
