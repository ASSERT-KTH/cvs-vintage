/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;

import javax.ejb.*;
import javax.transaction.UserTransaction;

import org.jboss.ejb.EnterpriseContext.EJBContextImpl;
import org.jboss.ejb.plugins.lock.NonReentrantLock;


/**
 * The EntityEnterpriseContext is used to associate EntityBean instances
 * with metadata about it.
 *
 * @see EnterpriseContext
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 1.41 $
 */
public class EntityEnterpriseContext extends EnterpriseContext
{
   private EJBObject ejbObject;
   private EJBLocalObject ejbLocalObject;
   private EntityContext ctx;
	
   /**
    * True if this instance has been registered with the TM for transactional
    * demarcation.
    */
   private boolean hasTxSynchronization = false;
	
   /**
    * True if this instances' state is valid when a bean is called the state
    * is not synchronized with the DB but "valid" as long as the transaction
    * runs.
    */
   private boolean valid = false;
	
   /**
    * Is this context in a readonly invocation.
    */
   private boolean readOnly = false;

   /**
    * The persistence manager may attach any metadata it wishes to this
    * context here.
    */
   private Object persistenceCtx;
	
   /** The cacheKey for this context */
   private Object key;

   private NonReentrantLock methodLock = new NonReentrantLock();

   public EntityEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
      ctx = new EntityContextImpl();
      try
      {
         AllowedOperationsAssociation.pushInMethodFlag(IN_SET_ENTITY_CONTEXT);
         ((EntityBean)instance).setEntityContext(ctx);
      }
      finally
      {
         AllowedOperationsAssociation.popInMethodFlag();
      }
   }
	
   /**
    * A non-reentrant deadlock detectable lock that is used to protected against
    * entity bean reentrancy.
    * @return
    */
   public NonReentrantLock getMethodLock()
   {
      return methodLock;
   }

   public void clear()
   {
      super.clear();
      
      hasTxSynchronization = false;
      valid = false;
      readOnly = false;
      key = null;
      persistenceCtx = null;
      ejbObject = null;
   }
	
   public void discard() throws RemoteException
   {
      ((EntityBean)instance).unsetEntityContext();
   }
	
   public EJBContext getEJBContext()
   {
      return ctx;
   }
	
   public void setEJBObject(EJBObject eo)
   {
      ejbObject = eo;
   }
	
   public EJBObject getEJBObject()
   {
      // Context can have no EJBObject (created by finds) in which case
      // we need to wire it at call time
      return ejbObject;
   }
	
   public void setEJBLocalObject(EJBLocalObject eo)
   {
      ejbLocalObject = eo;
   }
	
   public EJBLocalObject getEJBLocalObject()
   {
      return ejbLocalObject;
   }
	
   public void setCacheKey(Object key)
   {
      this.key = key;
   }
	
   public Object getCacheKey()
   {
      return key;
   }
	
   public void setPersistenceContext(Object ctx)
   {
      this.persistenceCtx = ctx;
   }
	
   public Object getPersistenceContext()
   {
      return persistenceCtx;
   }
	
   public void hasTxSynchronization(boolean value)
   {
      hasTxSynchronization = value;
   }
	
   public boolean hasTxSynchronization()
   {
      return hasTxSynchronization;
   }
	
   public void setValid(boolean valid)
   {
      this.valid = valid;
   }
	
   public boolean isValid()
   {
      return valid;
   }

	public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;
   }
	
   public boolean isReadOnly()
   {
      return readOnly;
   }

   public String toString()
   {
      return getContainer().getBeanMetaData().getEjbName() + '#' + getId();
   }

   protected class EntityContextImpl
      extends EJBContextImpl
      implements EntityContext
   {
      public EJBHome getEJBHome()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBHome",
                 IN_SET_ENTITY_CONTEXT | IN_UNSET_ENTITY_CONTEXT |
                 IN_EJB_CREATE | IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_FIND | IN_EJB_HOME |
                 IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         return super.getEJBHome();
      }

      public EJBLocalHome getEJBLocalHome()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBLocalHome",
                 IN_SET_ENTITY_CONTEXT | IN_UNSET_ENTITY_CONTEXT |
                 IN_EJB_CREATE | IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_FIND | IN_EJB_HOME |
                 IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         return super.getEJBLocalHome();
      }

      public Principal getCallerPrincipal()
      {
         AllowedOperationsAssociation.assertAllowedIn("getCallerPrincipal",
                 IN_EJB_CREATE | IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_FIND | IN_EJB_HOME |
                 IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         return super.getCallerPrincipal();
      }

      public boolean getRollbackOnly()
      {
         AllowedOperationsAssociation.assertAllowedIn("getRollbackOnly",
                 IN_EJB_CREATE | IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_FIND | IN_EJB_HOME |
                 IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         return super.getRollbackOnly();
      }

      public void setRollbackOnly()
      {
         AllowedOperationsAssociation.assertAllowedIn("setRollbackOnly",
                 IN_EJB_CREATE | IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_FIND | IN_EJB_HOME |
                 IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         super.setRollbackOnly();
      }

      public boolean isCallerInRole(String id)
      {
         AllowedOperationsAssociation.assertAllowedIn("getCallerInRole",
                 IN_EJB_CREATE | IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_FIND | IN_EJB_HOME |
                 IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);
         return super.isCallerInRole(id);
      }

      public UserTransaction getUserTransaction()
      {
         AllowedOperationsAssociation.assertAllowedIn("getUserTransaction", NOT_ALLOWED);
         return super.getUserTransaction();
      }

      public EJBObject getEJBObject()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBObject",
                 IN_EJB_POST_CREATE | IN_EJB_REMOVE |
                 IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         if(((EntityContainer)con).getRemoteClass() == null)
         {
            throw new IllegalStateException( "No remote interface defined." );
         }

         if (ejbObject == null)
         {   
            // Create a new CacheKey
            Object cacheKey = ((EntityCache)((EntityContainer)con).getInstanceCache()).createCacheKey(id);
            EJBProxyFactory proxyFactory = con.getProxyFactory();
            if(proxyFactory == null)
            {
               String defaultInvokerName = con.getBeanMetaData().
                  getContainerConfiguration().getDefaultInvokerName();
               proxyFactory = con.lookupProxyFactory(defaultInvokerName);
            }
            ejbObject = (EJBObject)proxyFactory.getEntityEJBObject(cacheKey);
         }

         return ejbObject;
      }
		
      public EJBLocalObject getEJBLocalObject()
      {
         AllowedOperationsAssociation.assertAllowedIn("getEJBLocalObject",
                 IN_EJB_POST_CREATE | IN_EJB_REMOVE |
                 IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         if (con.getLocalHomeClass()==null)
            throw new IllegalStateException( "No local interface for bean." );
         
         if (ejbLocalObject == null)
         {
            Object cacheKey = ((EntityCache)((EntityContainer)con).getInstanceCache()).createCacheKey(id);            
            ejbLocalObject = ((EntityContainer)con).getLocalProxyFactory().getEntityEJBLocalObject(cacheKey);
         }
         return ejbLocalObject;
      }
		
      public Object getPrimaryKey()
      {
         AllowedOperationsAssociation.assertAllowedIn("getPrimaryKey",
                 IN_EJB_POST_CREATE | IN_EJB_REMOVE |
                 IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         return id;
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         AllowedOperationsAssociation.assertAllowedIn("getTimerService",
                 IN_EJB_CREATE | IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_HOME |
                 IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_EJB_LOAD | IN_EJB_STORE | IN_BUSINESS_METHOD |
                 IN_EJB_TIMEOUT);

         return new TimerServiceWrapper(this, getContainer().getTimerService(id));
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
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(duration, info);
      }

      public Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(initialDuration, intervalDuration, info);
      }

      public Timer createTimer(Date expiration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(expiration, info);
      }

      public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws IllegalArgumentException, IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.createTimer");
         return timerService.createTimer(initialExpiration, intervalDuration, info);
      }

      public Collection getTimers() throws IllegalStateException, EJBException
      {
         assertAllowedIn("TimerService.getTimers");
         return timerService.getTimers();
      }

      private void assertAllowedIn(String timerMethod)
      {
         AllowedOperationsAssociation.assertAllowedIn(timerMethod,
                 IN_EJB_POST_CREATE | IN_EJB_REMOVE | IN_EJB_LOAD | IN_EJB_STORE |
                 IN_BUSINESS_METHOD | IN_EJB_TIMEOUT);
      }
   }
}