/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.AllowedOperationsAssociation;
import org.jboss.invocation.Invocation;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;

/**
 * The instance interceptors role is to acquire a context representing
 * the target object from the cache.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.17 $
 */
public class EntityMultiInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------
	
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
	
   // Interceptor implementation --------------------------------------
	
   public Object invokeHome(Invocation mi)
      throws Exception
   {
      // Get context
      EntityContainer ec = (EntityContainer) getContainer();
      EntityEnterpriseContext ctx = (EntityEnterpriseContext) ec.getInstancePool().get();

		// Pass it to the method invocation
      mi.setEnterpriseContext(ctx);

      // Give it the transaction
      ctx.setTransaction(mi.getTransaction());

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());

      AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_HOME);

      Object result;
      try
      {
         // Invoke through interceptors
         result = getNext().invokeHome(mi);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
      
      // No id, means we can put the context back in the pool
      if (ctx.getId() == null)
      {
         ctx.setTransaction(null);
         ec.getInstancePool().free(ctx);
      }
      
      // We are done
      return result;
   }

   public Object invoke(Invocation mi)
      throws Exception
   {

      // The key
      Object key = mi.getId();

      EntityEnterpriseContext ctx = null;
      EntityContainer ec = (EntityContainer) container;
      if (mi.getTransaction() != null)
      {
         ctx = ec.getTxEntityMap().getCtx(mi.getTransaction(), key);
      }
      if (ctx == null)
      {
         InstancePool pool = ec.getInstancePool();
         ctx = (EntityEnterpriseContext) pool.get();
         ctx.setCacheKey(key);
         ctx.setId(key);
         EntityPersistenceManager pm = ec.getPersistenceManager();
         pm.activateEntity(ctx);
      }

      boolean trace = log.isTraceEnabled();
      if( trace ) log.trace("Begin invoke, key="+key);

      // Associate transaction, in the new design the lock already has the transaction from the
      // previous interceptor
      ctx.setTransaction(mi.getTransaction());

      // Set the current security information
      ctx.setPrincipal(mi.getPrincipal());
      // Set the JACC EnterpriseBean PolicyContextHandler data
      EnterpriseBeanPolicyContextHandler.setEnterpriseBean(ctx.getInstance());

      // Set context on the method invocation
      mi.setEnterpriseContext(ctx);

      if (ejbTimeout.equals(mi.getMethod()))
         AllowedOperationsAssociation.pushInMethodFlag(IN_EJB_TIMEOUT);
      else
         AllowedOperationsAssociation.pushInMethodFlag(IN_BUSINESS_METHOD);

      try
      {
         Object ret = getNext().invoke(mi);
         return ret;
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }
}
