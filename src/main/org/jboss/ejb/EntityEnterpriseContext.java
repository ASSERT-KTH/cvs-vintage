/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBContext;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.TimerService;


/**
 * The EntityEnterpriseContext is used to associate EntityBean instances
 * with metadata about it.
 *
 * @see EnterpriseContext
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 1.30 $
 */
public class EntityEnterpriseContext extends EnterpriseContext
{
   EJBObject ejbObject;
   EJBLocalObject ejbLocalObject;
   EntityContext ctx;
	
   /**
    * True if this instance has been registered with the TM for transactional
    * demarcation.
    */
   boolean hasTxSynchronization = false;
	
   /**
    * True if this instances' state is valid when a bean is called the state
    * is not synchronized with the DB but "valid" as long as the transaction
    * runs.
    */
   boolean valid = false;
	
   /**
    * The persistence manager may attach any metadata it wishes to this
    * context here.
    */
   private Object persistenceCtx;
	
   public EntityEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
      ctx = new EntityContextImpl();
      ((EntityBean)instance).setEntityContext(ctx);
   }
	
   public void clear() 
   {
      super.clear();
      
      this.hasTxSynchronization = false;
      this.valid = false;
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
	
   protected class EntityContextImpl
      extends EJBContextImpl
      implements EntityContext
   {
      public EJBObject getEJBObject()
      {
         if(((EntityContainer)con).getProxyFactory() == null)
         {
            throw new IllegalStateException( "No remote interface defined." );
         }

         if(ejbObject == null)
         {   
            ejbObject = (EJBObject) ((EntityContainer)con).getProxyFactory().getEntityEJBObject(id);  
         }

         return ejbObject;
      }

      public EJBLocalObject getEJBLocalObject()
      {
         if(con.getLocalHomeClass()==null)
         {
            throw new IllegalStateException( "No local interface for bean." );
         }

         if(ejbLocalObject == null)
         {
            ejbLocalObject = ((EntityContainer)con).getLocalProxyFactory().getEntityEJBLocalObject(id);
         }
         return ejbLocalObject;
      }

      public Object getPrimaryKey()
      {
         return id;
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         return getContainer().getTimerService(id);
      }
   }
}
