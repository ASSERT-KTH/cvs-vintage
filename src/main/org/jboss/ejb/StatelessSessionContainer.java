/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import org.jboss.ejb.txtimer.TimedObjectInvoker;
import org.jboss.invocation.Invocation;
import org.jboss.proxy.ejb.EJBMetaDataImpl;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * The container for <em>stateless</em> session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>
 * @version $Revision: 1.59 $
 */
public class StatelessSessionContainer extends SessionContainer
        implements EJBProxyFactoryContainer, InstancePoolContainer
{
   // EJBObject implementation --------------------------------------

   /**
    * No-op.
    */
   public void remove(Invocation mi)
           throws RemoteException, RemoveException
   {
      log.debug("Useless invocation of remove() for stateless session bean");
   }

   // EJBLocalHome implementation

   public EJBLocalObject createLocalHome()
           throws CreateException
   {
      if (localProxyFactory == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }
      createCount++;
      return localProxyFactory.getStatelessSessionEJBLocalObject();
   }

   /**
    * No-op.
    */
   public void removeLocalHome(Object primaryKey)
   {
      log.debug("Useless invocation of remove(Object) for stateless session bean");
   }

   // EJBHome implementation ----------------------------------------

   public EJBObject createHome()
           throws RemoteException, CreateException
   {
      EJBProxyFactory ci = getProxyFactory();
      if (ci == null)
      {
         String msg = "No ProxyFactory, check for ProxyFactoryFinderInterceptor";
         throw new IllegalStateException(msg);
      }
      createCount++;
      Object obj = ci.getStatelessSessionEJBObject();
      return (EJBObject) obj;
   }

   /**
    * No-op.
    */
   public void removeHome(Handle handle)
           throws RemoteException, RemoveException
   {
      removeCount++;
      log.debug("Useless invocation of remove(Handle) for stateless session bean");
   }

   /**
    * No-op.
    */
   public void removeHome(Object primaryKey)
           throws RemoteException, RemoveException
   {
      removeCount++;
      log.debug("Useless invocation of remove(Object) for stateless session bean");
   }

   // Protected  ----------------------------------------------------

   protected void setupHomeMapping()
           throws NoSuchMethodException
   {
      boolean debug = log.isDebugEnabled();

      Map map = new HashMap();

      if (homeInterface != null)
      {
         Method[] m = homeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            // Implemented by container
            if (debug)
               log.debug("Mapping " + m[i].getName());
            map.put(m[i], getClass().getMethod(m[i].getName() + "Home", m[i].getParameterTypes()));
         }
      }
      if (localHomeInterface != null)
      {
         Method[] m = localHomeInterface.getMethods();
         for (int i = 0; i < m.length; i++)
         {
            // Implemented by container
            if (debug)
               log.debug("Mapping " + m[i].getName());
            map.put(m[i], getClass().getMethod(m[i].getName() + "LocalHome", m[i].getParameterTypes()));
         }
      }

      homeMapping = map;
   }

   Interceptor createContainerInterceptor()
   {
      return new ContainerInterceptor();
   }

   /**
    * This is the last step before invocation - all interceptors are done
    */
   class ContainerInterceptor
           extends AbstractContainerInterceptor
   {
      public Object invokeHome(Invocation mi) throws Exception
      {
         Method miMethod = mi.getMethod();
         Method m = (Method) getHomeMapping().get(miMethod);
         if (m == null)
         {
            String msg = "Invalid invocation, check your deployment packaging, method=" + miMethod;
            throw new EJBException(msg);
         }

         try
         {
            return mi.performCall(StatelessSessionContainer.this, m, mi.getArguments());
         }
         catch (Exception e)
         {
            rethrow(e);
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }

      public Object invoke(Invocation mi) throws Exception
      {
         // wire the transaction on the context, this is how the instance remember the tx
         EnterpriseContext ctx = (EnterpriseContext) mi.getEnterpriseContext();
         if (ctx.getTransaction() == null)
            ctx.setTransaction(mi.getTransaction());

         // Get method and instance to invoke upon
         Method miMethod = mi.getMethod();

         Map map = getBeanMapping();
         Method m = (Method) map.get(miMethod);
         if (m == null)
         {
            String msg = "Invalid invocation, check your deployment packaging, method=" + miMethod;
            throw new EJBException(msg);
         }

         //If we have a method that needs to be done by the container (EJBObject methods)
         if (m.getDeclaringClass().equals(StatelessSessionContainer.class) ||
               m.getDeclaringClass().equals(SessionContainer.class))
         {
            try
            {
               return mi.performCall(StatelessSessionContainer.this, m, new Object[]{mi});
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }
         else // we have a method that needs to be done by a bean instance
         {
            // Invoke and handle exceptions
            try
            {
               Object bean = ctx.getInstance();
               return mi.performCall(bean, m, mi.getArguments());
            }
            catch (Exception e)
            {
               rethrow(e);
            }
         }

         // We will never get this far, but the compiler does not know that
         throw new org.jboss.util.UnreachableStatementException();
      }
   }
}
