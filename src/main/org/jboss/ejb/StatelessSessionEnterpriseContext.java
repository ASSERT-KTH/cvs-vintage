/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

/**
 * The enterprise context for stateless session beans.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.24 $
 */
public class StatelessSessionEnterpriseContext
   extends EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   EJBObject ejbObject;
   EJBLocalObject ejbLocalObject;
   SessionContext ctx;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public StatelessSessionEnterpriseContext(Object instance, Container con)
      throws Exception
   {
      super(instance, con);
      ctx = new SessionContextImpl();

      try
      {
         pushInMethodFlag(IN_SET_SESSION_CONTEXT);
         ((SessionBean)instance).setSessionContext(ctx);
      }
      finally
      {
         popInMethodFlag();
      }

      try
      {
         pushInMethodFlag(IN_EJB_CREATE);
         Method ejbCreate = instance.getClass().getMethod("ejbCreate", new Class[0]);
         ejbCreate.invoke(instance, new Object[0]);
      } catch (InvocationTargetException e) 
      {
         Throwable ex = e.getTargetException();
         if (ex instanceof EJBException)
            throw (Exception)ex;
         else if (ex instanceof RuntimeException)
            throw new EJBException((Exception)ex); // Transform runtime exception into what a bean *should* have thrown
         else if (ex instanceof Exception)
            throw (Exception)ex;
         else
            throw (Error)ex;
      }
      finally
      {
         popInMethodFlag();
      }
   }
   
   // Public --------------------------------------------------------
   
   public void setEJBObject(EJBObject eo) {
      ejbObject = eo;
   }
   
   public EJBObject getEJBObject() {
      return ejbObject;
   }
   
   public void setEJBLocalObject(EJBLocalObject eo) {
      ejbLocalObject = eo;
   }
   
   public EJBLocalObject getEJBLocalObject() {
      return ejbLocalObject;
   }
   
   public SessionContext getSessionContext() {
      return ctx;
   }

   // EnterpriseContext overrides -----------------------------------
   
   public void discard() throws RemoteException
   {
      ((SessionBean)instance).ejbRemove();
   }
   
   public EJBContext getEJBContext()
   {
      return ctx;
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   
   protected class SessionContextImpl
      extends EJBContextImpl
      implements SessionContext
   {
      public EJBHome getEJBHome()
      {
         assertAllowedIn("getEJBHome",
                 IN_SET_SESSION_CONTEXT | IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);

         return super.getEJBHome();
      }

      public EJBLocalHome getEJBLocalHome()
      {
         assertAllowedIn("getEJBLocalHome",
                 IN_SET_SESSION_CONTEXT | IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);

         return super.getEJBLocalHome();
      }

      public EJBObject getEJBObject()
      {
         assertAllowedIn("getEJBObject", IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);

         if (((StatelessSessionContainer)con).getProxyFactory()==null)
            throw new IllegalStateException( "No remote interface defined." );
         
         if (ejbObject == null)
         {
            EJBProxyFactory proxyFactory = con.getProxyFactory();
            if(proxyFactory == null)
            {
               String defaultInvokerName = con.getBeanMetaData().
                  getContainerConfiguration().getDefaultInvokerName();
               proxyFactory = con.lookupProxyFactory(defaultInvokerName);
            }
            ejbObject = (EJBObject) proxyFactory.getStatelessSessionEJBObject(); 
         } 	
    
         return ejbObject;
      }

      public EJBLocalObject getEJBLocalObject()
      {
         assertAllowedIn("getEJBLocalObject", IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);

         if (con.getLocalHomeClass()==null)
            throw new IllegalStateException( "No local interface for bean." );
         if (ejbLocalObject == null) {
            ejbLocalObject = ((StatelessSessionContainer)con).getLocalProxyFactory().getStatelessSessionEJBLocalObject(); 
         }
         return ejbLocalObject;
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         assertAllowedIn("getTimerService", IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         return new TimerServiceWrapper(this, super.getTimerService());
      }

      public Principal getCallerPrincipal()
      {
         assertAllowedIn("getCallerPrincipal", IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         return super.getCallerPrincipal();
      }

      public boolean getRollbackOnly()
      {
         assertAllowedIn("getRollbackOnly", IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         return super.getRollbackOnly();
      }

      public void setRollbackOnly()
      {
         assertAllowedIn("setRollbackOnly", IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         super.setRollbackOnly();
      }

      public boolean isCallerInRole(String id)
      {
         assertAllowedIn("isCallerInRole", IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         return super.isCallerInRole(id);
      }

      public UserTransaction getUserTransaction()
      {
         assertAllowedIn("getUserTransaction", IN_EJB_CREATE | IN_EJB_REMOVE | IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
         return super.getUserTransaction();
      }

      public MessageContext getMessageContext() throws IllegalStateException
      {
         assertAllowedIn("getMessageContext", IN_SERVICE_ENDPOINT_METHOD);
         return null;
      }
   }

   /**
    * Delegates to the underlying TimerService, after checking access
    */
   public class TimerServiceWrapper implements TimerService
   {

      private EnterpriseContext.EJBContextImpl context;
      private TimerService timerService;

      public TimerServiceWrapper(EnterpriseContext.EJBContextImpl ctx, TimerService timerService)
      {
         this.context = ctx;
         this.timerService = timerService;
      }

      public Timer createTimer(long duration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("createTimer");
         return timerService.createTimer(duration, info);
      }

      public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("createTimer");
         return timerService.createTimer(initialDuration, intervalDuration, info);
      }

      public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("createTimer");
         return timerService.createTimer(expiration, info);
      }

      public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("createTimer");
         return timerService.createTimer(initialExpiration, intervalDuration, info);
      }

      public Collection getTimers() throws IllegalStateException, EJBException
      {
         assertAllowedIn("getTimers");
         return timerService.getTimers();
      }

      private void assertAllowedIn(String timerMethod)
      {
         context.assertAllowedIn(timerMethod, IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
      }
   }
}

