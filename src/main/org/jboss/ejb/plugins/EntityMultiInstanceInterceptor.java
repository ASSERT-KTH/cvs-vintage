/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;



import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.CacheKey;

/**
 * The instance interceptors role is to acquire a context representing
 * the target object from the cache.
 *
 *    
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.3 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2001/08/08: billb</b>
 * <ol>
 *   <li>Initial Revision
 * </ol>
 */
public class EntityMultiInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
	
   // Attributes ----------------------------------------------------
	
   protected EntityContainer container;
	
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
   }
	
   public Container getContainer()
   {
      return container;
   }
	
   // Interceptor implementation --------------------------------------
	
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      // Get context
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)((EntityContainer)getContainer()).getInstancePool().get();
		
		// Pass it to the method invocation
      mi.setEnterpriseContext(ctx);
		
      // Give it the transaction
      ctx.setTransaction(mi.getTransaction());
		
      // Invoke through interceptors
      return getNext().invokeHome(mi);
   }
	
   public Object invoke(MethodInvocation mi)
      throws Exception
   {
		
      // The key
      CacheKey key = (CacheKey) mi.getId();

      EntityEnterpriseContext ctx = null;
      if (mi.getTransaction() != null)
      {
         ctx = container.getTxEntityMap().getCtx(mi.getTransaction(), key);
      }
      if (ctx == null)
      {
         ctx = (EntityEnterpriseContext)container.getInstancePool().get();
         ctx.setCacheKey(key);
         ctx.setId(key.getId());
         container.getPersistenceManager().activateEntity(ctx);
      }
		
      boolean trace = log.isTraceEnabled();
      if( trace ) log.trace("Begin invoke, key="+key);
			
      // Associate transaction, in the new design the lock already has the transaction from the 
      // previous interceptor
      ctx.setTransaction(mi.getTransaction());
		
      // Set context on the method invocation
      mi.setEnterpriseContext(ctx);
		
      return getNext().invoke(mi);
   }
	
}



