/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.util.Map;

import org.jboss.ejb.Container;
import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.invocation.Invocation;
import org.jboss.ejb.EnterpriseContext;

import org.jboss.security.SecurityAssociation;

/**
 * This container acquires the given instance. This must be used after
 * the EnvironmentInterceptor, since acquiring instances requires a proper
 * JNDI environment to be set.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.12 $
 */
public class MessageDrivenInstanceInterceptor
   extends AbstractInterceptor
{
   /** The container for this interceptor. */
   protected MessageDrivenContainer container;

   /**
    * Set the container for this interceptor.
    *
    * @param container    A <tt>MessageDrivenContainer</tt>.
    *
    * @throws ClassCastException    Not a <tt>MessageDrivenContainer</tt>.
    */
   public void setContainer(final Container container)
   {
      this.container = (MessageDrivenContainer)container;
   }

   /**
    * Return the container for this interceptor.
    *
    * @return   The <tt>MessageDrivenContainer</tt> for this interceptor.
    */
   public Container getContainer()
   {
      return container;
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
      EnterpriseContext ctx = container.getInstancePool().get();

      // Set the current security information
      ctx.setPrincipal(SecurityAssociation.getPrincipal());

      // Use this context
      mi.setEnterpriseContext(ctx);

      // There is no need for synchronization since the instance is always
      // fresh also there should never be a tx associated with the instance.

      try
      {
         // Invoke through interceptors
         return getNext().invoke(mi);
      } catch (RuntimeException e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      } catch (RemoteException e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      } catch (Error e) // Instance will be GC'ed at MI return
      {
         mi.setEnterpriseContext(null);
         throw e;
      } finally
      {
         // Return context
         if ( mi.getEnterpriseContext() != null)
            container.getInstancePool().free((EnterpriseContext) mi.getEnterpriseContext());
      }
   }
  // Monitorable implementation ------------------------------------
  public void sample(Object s)
  {
    // Just here to because Monitorable request it but will be removed soon
  }
  public Map retrieveStatistic()
  {
    return null;
  }
  public void resetStatistic()
  {
  }
}

