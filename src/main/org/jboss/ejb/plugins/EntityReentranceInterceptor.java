/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationType;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.BeanMetaData;
import javax.ejb.EJBObject;
import javax.ejb.EJBException;
import java.rmi.RemoteException;
import org.jboss.ejb.plugins.lock.Entrancy;

/**
 * The role of this interceptor is to check for reentrancy.
 * Per the spec, throw an exception if instance is not marked
 * as reentrant.
 *
 * <p><b>WARNING: critical code</b>, get approval from senior developers
 * before changing.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.5 $
 */
public final class EntityReentranceInterceptor extends AbstractInterceptor
{
   private boolean reentrant = false;

   public void start()
   {
      EntityMetaData meta = (EntityMetaData)getContainer().getBeanMetaData();
      reentrant = meta.isReentrant();
   }

   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      if(invocation.getType().isHome()) 
      {
         return getNext().invoke(invocation);
      }
      
      if(isNonEntrantMethod(invocation))
      {
         return getNext().invoke(invocation);
      }

      // We are going to work with the context a lot
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext)invocation.getEnterpriseContext();

      // Not a reentrant method like getPrimaryKey
      synchronized(ctx)
      {
         if(!reentrant && ctx.isLocked())
         {
            if(invocation.getType() == InvocationType.REMOTE)
            {
               throw new RemoteException("Reentrant method call detected: " 
                     + getContainer().getBeanMetaData().getEjbName() + " " 
                     + ctx.getId().toString());
            }
            else
            {
               throw new EJBException("Reentrant method call detected: " 
                     + getContainer().getBeanMetaData().getEjbName() + " " 
                     + ctx.getId().toString());
            }
         }
         ctx.lock();
      }

      try
      {
         return getNext().invoke(invocation); 
      }
      finally
      {
         synchronized (ctx)
         {
            ctx.unlock();
         }
      }
   }

   private static final Method getEJBHome;
   private static final Method getHandle;
   private static final Method getPrimaryKey;
   private static final Method isIdentical;
   private static final Method remove;

   static
   {
      try
      {
         getEJBHome = EJBObject.class.getMethod("getEJBHome", null);
         getHandle = EJBObject.class.getMethod("getHandle", null);
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", null);
         isIdentical = EJBObject.class.getMethod(
               "isIdentical", 
               new Class[] { EJBObject.class });
         remove = EJBObject.class.getMethod("remove", null);
      }
      catch (Exception e) 
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }

   private boolean isNonEntrantMethod(Invocation invocation)
   {
      // is this a known non-entrant method
      Method method = invocation.getMethod();
      if(method == getEJBHome ||
            method == getHandle ||
            method == getPrimaryKey ||
            method == isIdentical ||
            method == remove)
      {
         return true;
      }

      // if this is a non-entrant message to the container let it through
      Entrancy entrancy = (Entrancy)invocation.getValue(Entrancy.ENTRANCY_KEY);
      if(entrancy == Entrancy.NON_ENTRANT)
      {
         log.trace("NON_ENTRANT invocation");
         return true;
      }
      return false;
   }
}
