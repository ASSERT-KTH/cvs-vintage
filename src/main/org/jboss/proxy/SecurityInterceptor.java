/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.Stack;
import java.util.EmptyStackException;
import java.security.PrivilegedAction;
import java.security.AccessController;

import org.jboss.logging.Logger;

/** An encapsulation of thread context class loader PrivilegedAction for
 * getting and setting the TCL. 
 * 
 * @version $Revision: 1.8 $
 * @author Scott.Stark@jboss.org
 */
class TCLStack
{
   private static final Logger log = Logger.getLogger(TCLStack.class);

   /** The thread local stack of class loaders. */
   private static final ThreadLocal stackTL = new ThreadLocal()
   {
      protected Object initialValue()
      {
         return new Stack();
      }
   };

   /** Get the stack from the thread lcoal. */
   private static Stack getStack()
   {
      return (Stack) stackTL.get();
   }

   /** Push the current TCL and set the given CL to the TCL. If the cl
    * argument is null then the current TCL is not updated and pop will leave
    * the current TCL the same as entry into push.
    *
    * @param cl - The class loader to set as the TCL.
    */
   static void push(final ClassLoader cl)
   {
      boolean trace = log.isTraceEnabled();

      // push the old cl and set the new cl
      ClassLoader oldCL = GetTCLAction.getContextClassLoader();
      if( cl != null )
      {
         SetTCLAction.setContextClassLoader(cl);
      }
      getStack().push(oldCL);

      if (trace)
      {
         log.trace("Setting TCL to " + cl + "; pushing " + oldCL);
         log.trace("Stack: " + getStack());
      }
   }

   /**
    * Pop the last CL from the stack and make it the TCL.
    *
    * <p>If the stack is empty, then no change is made to the TCL.
    *
    * @return   The previous CL or null if there was none.
    */
   static ClassLoader pop()
   {
      // get the last cl in the stack & make it the current
      try
      {
         ClassLoader cl = (ClassLoader) getStack().pop();
         ClassLoader oldCL = GetTCLAction.getContextClassLoader();

         SetTCLAction.setContextClassLoader(cl);

         if (log.isTraceEnabled())
         {
            log.trace("Setting TCL to " + cl + "; popped: " + oldCL);
            log.trace("Stack: " + getStack());
         }

         return oldCL;
      }
      catch (EmptyStackException ignore)
      {
         log.warn("Attempt to pop empty stack ingored", ignore);
         return null;
      }
   }
   
   /**
    * Return the size of the TCL stack.
    */
   static int size()
   {
      return getStack().size();
   }

   /**
    * Return the CL in the stack at the given index.
    */
   static ClassLoader get(final int index) throws ArrayIndexOutOfBoundsException
   {
      return (ClassLoader) getStack().get(index);
   }

   static ClassLoader getContextClassLoader()
   {
      return GetTCLAction.getContextClassLoader();      
   }

   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
      static ClassLoader getContextClassLoader()
      {
         ClassLoader loader = (ClassLoader) AccessController.doPrivileged(ACTION);
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
      static void setContextClassLoader(ClassLoader loader)
      {
         PrivilegedAction action = new SetTCLAction(loader);
         AccessController.doPrivileged(action);
      }
   }
}
