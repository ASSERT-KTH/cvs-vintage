/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.RemoveException;
import javax.ejb.EntityBean;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;

/**
 *   This container acquires the given instance. 
 *
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class EntityInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation --------------------------------------
   public Object invokeHome(Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      // Get context
      ctx = ((EntityContainer)getContainer()).getInstancePool().get();
      
      try
      {
         // Invoke through interceptors
         return getNext().invokeHome(method, args, ctx);
      } finally
      {
         // Still free? Not free if create() was called successfully
         if (ctx.getId() == null)
         {
            getContainer().getInstancePool().free(ctx);
         } else
         {
//            System.out.println("Entity was created; not returned to pool");
            ((EntityContainer)getContainer()).getInstanceCache().release(ctx);
         }
      }
   }

   public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      // Get context
      ctx = ((EntityContainer)getContainer()).getInstanceCache().get(id);
      try
      {
         // Invoke through interceptors
         return getNext().invoke(id, method, args, ctx);
      } catch (RemoteException e)
		{
			// Discard instance
			// EJB 1.1 spec 12.3.1
			((EntityContainer)getContainer()).getInstanceCache().remove(id);
			ctx = null;
			
			throw e;
		} catch (RuntimeException e)
		{
			// Discard instance
			// EJB 1.1 spec 12.3.1
			((EntityContainer)getContainer()).getInstanceCache().remove(id);
			ctx = null;
			
			throw e;
		} catch (Error e)
		{
			// Discard instance
			// EJB 1.1 spec 12.3.1
			((EntityContainer)getContainer()).getInstanceCache().remove(id);
			ctx = null;
			
			throw e;
		} finally
      {
//         System.out.println("Release instance for "+id);
			if (ctx != null)
			{
				if (ctx.getId() == null)
				{
				   // Remove from cache
				   ((EntityContainer)getContainer()).getInstanceCache().remove(id);
				   
				   // It has been removed -> send to free pool
				   getContainer().getInstancePool().free(ctx);
				}
				{
				   // Return context
				   ((EntityContainer)getContainer()).getInstanceCache().release(ctx);
				}
			}
      }
   }
}

