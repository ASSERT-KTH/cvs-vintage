/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.Container;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.util.NestedError;

/**
 * This interceptor calls the callback method or throws an 
 * IllegalArgumentException if there is no callback method in the invocation 
 * object.  This should be the last interceptro in the chain.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public final class CallbackInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      Method callback = (Method)
            invocation.getValue(InvocationKey.CALLBACK_METHOD);
      if(callback == null)
      {
         throw new IllegalArgumentException("Invocation does not " +
               "contain a callback method");
      }

      EnterpriseContext ctx = (EnterpriseContext)
            invocation.getEnterpriseContext();
      if(ctx == null)
      {
         throw new IllegalArgumentException("Invocation does not " +
               "contain an enterprise context");
      }

      Object[] callbackArgs = (Object[])
            invocation.getValue(InvocationKey.CALLBACK_ARGUMENTS);

      try
      {
         Object obj = callback.invoke(ctx.getInstance(), callbackArgs);
         return new InvocationResponse(obj);
      }
      catch (IllegalAccessException e)
      {
         // This method is using the Java language access control and the 
         // underlying method is inaccessible.
         throw new EJBException(e);
      }
      catch (InvocationTargetException e)
      {
         // unwrap the exception
         Throwable t = e.getTargetException();
         if(t instanceof Exception)
         {
            throw (Exception)t;
         }
         else if(t instanceof Error)
         {
            throw (Error)t;
         }
         else 
         {
            throw new NestedError("Unexpected Throwable", t);
         }
      }
   }
}
