/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.lang.reflect.Method;

import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.invocation.Invocation;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstancePool;

import javax.ejb.TimedObject;
import javax.ejb.Timer;

/**
 * This container acquires the given instance. This must be used after
 * the EnvironmentInterceptor, since acquiring instances requires a proper
 * JNDI environment to be set.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.17 $
 */
public class MessageDrivenInstanceInterceptor
      extends AbstractInterceptor
{

   protected Method ejbTimeout;

   public void create() throws Exception
   {
      super.create();
      ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
   }

   /**
    * Message driven beans do not have homes.
    *
    * @throws Error    Not valid for MessageDriven beans.
    */
   public Object invokeHome(final Invocation mi)
         throws Exception
   {
      throw new Error("Not valid for MessageDriven beans");
   }

   // Interceptor implementation --------------------------------------

   public Object invoke(final Invocation mi)
         throws Exception
   {
      // Get context
      MessageDrivenContainer mdc = (MessageDrivenContainer) container;
      InstancePool pool = mdc.getInstancePool();
      EnterpriseContext ctx = pool.get();

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());

      // Use this context
      mi.setEnterpriseContext(ctx);

      if (ejbTimeout.equals(mi.getMethod()))
         ctx.pushInMethodFlag(EnterpriseContext.IN_EJB_TIMEOUT);
      else
         ctx.pushInMethodFlag(EnterpriseContext.IN_BUSINESS_METHOD);

      // There is no need for synchronization since the instance is always
      // fresh also there should never be a tx associated with the instance.
      try
      {
         // Invoke through interceptors
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
         ctx.popInMethodFlag();
         
         // Return context
         if (mi.getEnterpriseContext() != null)
         {
            pool.free((EnterpriseContext) mi.getEnterpriseContext());
         }
         else
         {
            pool.discard(ctx);
         }
      }
   }

}

