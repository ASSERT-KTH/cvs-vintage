/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.proxy.ejb;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.lang.reflect.Method;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.naming.InitialContext;
import javax.naming.Name;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.handle.StatefulHandleImpl;

/**
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.6 $
 */
public class StatefulSessionInterceptor
   extends GenericEJBInterceptor
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   // private static final long serialVersionUID = 1379411137308931705L;
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * No-argument constructor for externalization.
    */
   public StatefulSessionInterceptor()
   {
   }

   // Public --------------------------------------------------------
   
   /**
    * InvocationHandler implementation.
    *
    * @throws Throwable    Any exception or error thrown while processing.
    */
   public InvocationResponse invoke(Invocation invocation)
      throws Throwable
   {
      InvocationContext ctx = invocation.getInvocationContext();
      
      Method m = invocation.getMethod();
      
      // Implement local methods
      if (m.equals(TO_STRING))
      {
         return new InvocationResponse(toString(ctx));
      }
      else if (m.equals(EQUALS))
      {
         Object[] args = invocation.getArguments();
         String argsString = args[0] != null ? args[0].toString() : "";
         String thisString = toString(ctx);
         return new InvocationResponse(new Boolean(thisString.equals(argsString)));
      }
      else if (m.equals(HASH_CODE))
      {
         return new InvocationResponse(new Integer(ctx.getCacheId().hashCode()));
      }
      // Implement local EJB calls
      else if (m.equals(GET_HANDLE))
      {
         int objectName = ((Integer) ctx.getObjectName()).intValue();
         String jndiName = (String) ctx.getValue(InvocationKey.JNDI_NAME);
         Invoker invoker = ctx.getInvoker();
         Object id = ctx.getCacheId();
         return new InvocationResponse(new StatefulHandleImpl(
               objectName, 
               jndiName, 
               invoker, 
               ctx.getInvokerProxyBinding(), 
               id));
      }
      else if (m.equals(GET_EJB_HOME))
      {
         return new InvocationResponse(getEJBHome(invocation));
      }
      else if (m.equals(GET_PRIMARY_KEY))
      {
         return new InvocationResponse(ctx.getCacheId());
      }
      else if (m.equals(IS_IDENTICAL))
      {
         Object[] args = invocation.getArguments();
         String argsString = args[0].toString();
         String thisString = toString(ctx);
         return new InvocationResponse(new Boolean(thisString.equals(argsString)));
      }
      // If not taken care of, go on and call the container
      else
      {
         // It is a remote invocation
         invocation.setType(InvocationType.REMOTE);
         
         // On this entry in cache
         invocation.setId(ctx.getCacheId());
         
         return getNext().invoke(invocation);
      }
   }

   private String toString(InvocationContext ctx)
   {
      return ctx.getValue(InvocationKey.JNDI_NAME) + ":" + 
            ctx.getCacheId().toString();
   }
}
