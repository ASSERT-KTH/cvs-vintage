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

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.handle.EntityHandleImpl;

/**
 * An EJB entity bean proxy class.
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.7 $
 */
public class EntityInterceptor
extends GenericEJBInterceptor
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   //   private static final long serialVersionUID = -1523442773137704949L;
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * No-argument constructor for externalization.
    */
   public EntityInterceptor()
   {}
   
   // Public --------------------------------------------------------
   
   /**
    * InvocationHandler implementation.
    *
    * @param proxy   The proxy object.
    * @param m       The method being invoked.
    * @param args    The arguments for the method.
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
         String jndiName = (String) ctx.getValue(InvocationKey.JNDI_NAME);
         Object id = ctx.getCacheId();
         return new InvocationResponse(new EntityHandleImpl(jndiName, id));
      }
      else if (m.equals(GET_PRIMARY_KEY))
      {
         return new InvocationResponse(ctx.getCacheId());
      }
      else if (m.equals(GET_EJB_HOME))
      {
         return new InvocationResponse(getEJBHome(invocation));
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
         // We are a Remote invocation
         invocation.setType(InvocationType.REMOTE);
         // We pertain to this ID (represented by cache ID)
         invocation.setId(ctx.getCacheId());
         return getNext().invoke(invocation);
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------//////
   private String toString(InvocationContext ctx)
   {
      return ctx.getValue(InvocationKey.JNDI_NAME) + ":" + 
            ctx.getCacheId().toString();
   }
   
}
