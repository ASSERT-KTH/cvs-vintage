/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.lang.reflect.Method;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionContainer;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.ejb.StatelessSessionEnterpriseContext;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.xml.rpc.handler.MessageContext;

/**
 * This container acquires the given instance. This must be used after
 * the EnvironmentInterceptor, since acquiring instances requires a proper
 * JNDI environment to be set
 *
 * @author Rickard Oberg
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.25 $
 */
public class StatelessSessionInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   protected StatelessSessionContainer container;

   // Static --------------------------------------------------------

   /** A reference to {@link javax.ejb.TimedObject#ejbTimeout}. */
   protected static final Method ejbTimeout;
   static
   {
      try
      {
         ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
      }
      catch (Exception e)
      {
         throw new ExceptionInInitializerError(e);
      }
   }

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   public void setContainer(final Container container) 
   {
      super.setContainer(container);
      this.container = (StatelessSessionContainer)container;
   }

   // Interceptor implementation --------------------------------------
   
   public Object invokeHome(final Invocation mi) throws Exception
   {
      InstancePool pool = container.getInstancePool();
      StatelessSessionEnterpriseContext ctx = null;
      try
      {
         // Acquire an instance in case the ejbCreate throws a CreateException  
         ctx = (StatelessSessionEnterpriseContext) pool.get();
         mi.setEnterpriseContext(ctx);
         // Dispatch the method to the container
         return getNext().invokeHome(mi);
      }
      finally
      {
         mi.setEnterpriseContext(null);
         // If an instance was created, return it to the pool
         if( ctx != null )
            pool.free(ctx);
      }

   }

   public Object invoke(final Invocation mi) throws Exception
   {
      // Get context
      InstancePool pool = container.getInstancePool();
      StatelessSessionEnterpriseContext ctx = (StatelessSessionEnterpriseContext)pool.get();

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());
      // Set the JACC EnterpriseBean PolicyContextHandler data
      EnterpriseBeanPolicyContextHandler.setEnterpriseBean(ctx.getInstance());

      // Use this context
      mi.setEnterpriseContext(ctx);

      // Timer invocation
      if (ejbTimeout.equals(mi.getMethod()))
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_TIMEOUT);
      }

      // Service Endpoint invocation
      else if (mi.getValue(InvocationKey.SOAP_MESSAGE_CONTEXT) != null)
      {
         ctx.setMessageContext((MessageContext)mi.getValue(InvocationKey.SOAP_MESSAGE_CONTEXT));
         AllowedOperationsAssociation.pushInMethodFlag(IN_SERVICE_ENDPOINT_METHOD);
      }

      // Business Method Invocation
      else
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_BUSINESS_METHOD);
      }

      // There is no need for synchronization since the instance is always fresh also there should
      // never be a tx associated with the instance.
      try
      {
         Object obj = getNext().invoke(mi);
         return obj;

      }
      catch (RuntimeException e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      }
      catch (RemoteException e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      }
      catch (Error e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();

         // Return context
         if (mi.getEnterpriseContext() != null)
         {
            pool.free(((EnterpriseContext) mi.getEnterpriseContext()));
         }
         else
         {
            pool.discard(ctx);
         }
      }
   }
}
