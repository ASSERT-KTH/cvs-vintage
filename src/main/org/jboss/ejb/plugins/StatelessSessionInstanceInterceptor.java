/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.ejb.EnterpriseContext;

/**
 * This container acquires the given instance. This must be used after
 * the EnvironmentInterceptor, since acquiring instances requires a proper
 * JNDI environment to be set.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.17 $
 */
public class StatelessSessionInstanceInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(final Invocation invocation) throws Exception
   {
      if(invocation.getType().isHome()) 
      {
         getNext().invoke(invocation);
      }

      // Get context
      EnterpriseContext ctx = getContainer().getInstancePool().get();

      // Set the current security information
      ctx.setPrincipal(invocation.getPrincipal());

      // Use this context
      invocation.setEnterpriseContext(ctx);

      // There is no need for synchronization since the instance is always
      // fresh also there should never be a tx associated with the instance.

      try
      {
         return getNext().invoke(invocation);
      }
      catch(RuntimeException e) 
      {
         // Instance will be GC'ed at MI return 
         invocation.setEnterpriseContext(null);
         throw e;
      }
      catch(RemoteException e)
      {
         // Instance will be GC'ed at MI return 
         invocation.setEnterpriseContext(null);
         throw e;
      }
      catch(Error e)
      {
         // Instance will be GC'ed at MI return 
         invocation.setEnterpriseContext(null);
         throw e;
      }
      finally
      {
         // Return context
         if(invocation.getEnterpriseContext() != null)
         {
            getContainer().getInstancePool().free(
                  (EnterpriseContext)invocation.getEnterpriseContext());
         }
         else
         {
            getContainer().getInstancePool().discard(ctx);
         }
      }
   }
}
