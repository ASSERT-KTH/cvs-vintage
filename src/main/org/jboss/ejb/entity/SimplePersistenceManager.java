/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.ClassLoader;
import java.lang.reflect.Method;
import java.util.Collection;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerPlugin;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;
import org.jboss.logging.Logger;

/**
 * The SimplePersistenceManager is called by other plugins in the
 * container.  
 *
 * see EntityContainer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public class SimplePersistenceManager implements EntityPersistenceManager
{
   /**
    * The container for which this interceptor is intercepting calls.
    */
   private Container container;

   /** 
    * Logging instance 
    */
   protected Logger log = Logger.getLogger(this.getClass());

   /**
    * This is the first interceptor in the chain. 
    */
   private Interceptor interceptor;

   public final void setContainer(Container container)
   {
      this.container = container;
   }
   
   public final Container getContainer()
   {
      return container;
   }
   
   public void create() throws Exception 
   {
      Interceptor in = interceptor;
      while(in != null)
      {
         in.setContainer(getContainer());
         in.create();
         in = in.getNext();
      }
   }

   public void start() throws Exception
   {
      Interceptor in = interceptor;
      while(in != null)
      {
         in.start();
         in = in.getNext();
      }
   }

   public void stop()
   {
      Interceptor in = interceptor;
      while(in != null)
      {
         in.stop();
         in = in.getNext();
      }
   }

   public void destroy()
   {
      Interceptor in = interceptor;
      while(in != null)
      {
         in.destroy();
         in.setContainer(null);
         in = in.getNext();
      }
   }

   public final Interceptor getInterceptor()
   {
      return interceptor;
   }

   public final void addInterceptor(Interceptor newInterceptor)
   {
      if (interceptor == null)
      {
         interceptor = newInterceptor;
      }
      else
      {
         Interceptor current = interceptor;
         while(current.getNext() != null)
         {
            current = current.getNext();
         }
         
         current.setNext(newInterceptor);
      }
   }
 
   public final InvocationResponse invoke(Invocation invocation) throws Exception
   {
      // Associate thread with classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
      try
      {
         return getInterceptor().invoke(invocation);
      }
      finally
      {
         // Reset classloader
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
 
   public final Object createEntityInstance() throws Exception
   {
      return invoke(createInvocation(LifeCycleEvent.CREATE_INSTANCE)).getResponse();
   }

   public final Object createEntity(
         Method method, 
         Object[] arguments, 
         EntityEnterpriseContext ctx) throws Exception
   {
      return invoke(createInvocation(
               LifeCycleEvent.CREATE, 
               method, 
               arguments, 
               ctx)).getResponse();
   }

   public final void postCreateEntity(
         Method method, 
         Object[] arguments, 
         EntityEnterpriseContext ctx) throws Exception
   {
      invoke(createInvocation(
               LifeCycleEvent.POST_CREATE, 
               method, 
               arguments, 
               ctx));
   }
   
   public final void removeEntity(EntityEnterpriseContext ctx) throws Exception
   {
      if(ctx.getId() == null)
      {
         // entitiy has already been deleted; ignore delete request
         return;
      }

      invoke(createInvocation(LifeCycleEvent.REMOVE, ctx));
   }

   public final Collection query(
         Method method, 
         Object[] arguments, 
         EntityEnterpriseContext ctx) throws Exception
   {
      return (Collection)invoke(createInvocation(
               LifeCycleEvent.QUERY, 
               method, 
               arguments, 
               ctx)).getResponse();
   }
      
   public final boolean isEntityModified(EntityEnterpriseContext ctx) throws Exception
   {
      Object returnValue = invoke(createInvocation(
               LifeCycleEvent.IS_MODIFIED, 
               ctx)).getResponse();
      return ((Boolean)returnValue).booleanValue();
   }

   public final void loadEntity(EntityEnterpriseContext ctx) throws Exception
   {
      invoke(createInvocation(LifeCycleEvent.LOAD, ctx));
   }

   public final void storeEntity(EntityEnterpriseContext ctx) throws Exception
   {
      if(ctx.getId() == null)
      {
         // entitiy has been deleted; ignore store request
         return;
      }

      if(!isEntityModified(ctx)) 
      {
         return;
      }

      invoke(createInvocation(LifeCycleEvent.STORE, ctx));
   }

   public final void activateEntity(EntityEnterpriseContext ctx) throws Exception
   {
      invoke(createInvocation(LifeCycleEvent.ACTIVATE, ctx));
   }

   public final void passivateEntity(EntityEnterpriseContext ctx) throws Exception
   {
      invoke(createInvocation(LifeCycleEvent.PASSIVATE, ctx));
   }

   private Invocation createInvocation(LifeCycleEvent event)
         throws Exception
   {
      return createInvocation(event, null, null, null);
   }

   private Invocation createInvocation(
         LifeCycleEvent event,
         EntityEnterpriseContext ctx) throws Exception
   {
      return createInvocation(event, null, null, ctx);
   }

   private Invocation createInvocation(
         LifeCycleEvent event,
         Method method, 
         Object[] arguments, 
         EntityEnterpriseContext ctx) throws Exception
   {
      Invocation invocation = new Invocation();
      LifeCycleEvent.set(invocation, event);

      if(method != null)
      {
         invocation.setMethod(method);
         Class interfaceClass = method.getDeclaringClass();
         if(EJBLocalHome.class.isAssignableFrom(interfaceClass))
         {
            invocation.setType(InvocationType.LOCALHOME);
         }
         else if(EJBLocalObject.class.isAssignableFrom(interfaceClass))
         {
            invocation.setType(InvocationType.LOCAL);
         }
         else if(EJBHome.class.isAssignableFrom(interfaceClass))
         {
            invocation.setType(InvocationType.HOME);
         }
         else if(EJBObject.class.isAssignableFrom(interfaceClass))
         {
            invocation.setType(InvocationType.REMOTE);
         }
      }

      if(arguments != null)
      {
         invocation.setArguments(arguments);
      }

      if(ctx != null)
      {
         invocation.setEnterpriseContext(ctx);
         invocation.setId(ctx.getId());
         invocation.setTransaction(ctx.getTransaction());
      }
      return invocation;
   }
}

