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
 * @version $Revision: 1.31 $
 */
public class EntityEnterpriseContext extends EnterpriseContext
{
   private EJBObject ejbObject;
   private EJBLocalObject ejbLocalObject;
   private EntityContext ctx;
	
   /**
    * True if this instances' state is valid when a bean is called the state
    * is not synchronized with the DB but "valid" as long as the transaction
    * runs.
    */
   private boolean valid = false;
	
   /**
    * The persistence manager may attach any metadata it wishes to this
    * context here.
    */
   private Object persistenceCtx;
	
   /**
    * Is this context in the middle of a store opperation?
    */
   private boolean inStore = false;

   /**
    * Is the entity in a readonly invocation.
    */
   private boolean readOnly = false;

   public EntityEnterpriseContext(Object instance, Container container)
      throws RemoteException
   {
      super(instance, container);
      ctx = new EntityContextImpl();
      ((EntityBean)instance).setEntityContext(ctx);
   }
	
   public void clear() 
   {
      super.clear();
      
      valid = false;
      persistenceCtx = null;
      inStore = false;
      ejbObject = null;
      readOnly = false;
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
	
   public void setValid(boolean valid)
   {
      this.valid = valid;
   }
	
   public boolean isValid()
   {
      return valid;
   }
	
   public boolean isInStore()
   {
      return inStore;
   }

   public void setInStore(final boolean inStore)
   {
      this.inStore = inStore;
   }

   public boolean isReadOnly()
   {
      return readOnly;
   }

   public void setReadOnly(final boolean readOnly)
   {
      this.readOnly = readOnly;
   }


   /**
    * Two EntityEnterpriseContexts are the equal if they 
    * both are from the same container and have the same
    * id.
    * @param o the reference object with which to compare
    * @return true if this object is the same as the object
    */
   public boolean equals(Object o)
   {
      if(o instanceof EntityEnterpriseContext)
      {
         EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext)o;
         return (container == ctx.container) &&
            (id != null && id.equals(ctx.id));
      }
      return false;
   }
   
   public int hashCode()
   {
      int result = 17;
      result = 37*result + container.hashCode();
      if(id == null)
      {
         result = 37*result + 987899309;
      }
      else
      {
         result = 37*result + id.hashCode();
      }
      return result;
   }

   public String toString()
   {
      return "[EntityEnterpriseContext :" +
         " ejb=" + container.getBeanMetaData().getEjbName() + 
         " id=" + id + "]";
   }

   protected class EntityContextImpl
      extends EJBContextImpl
      implements EntityContext
   {
      public EJBObject getEJBObject()
      {
         if(((EntityContainer)container).getProxyFactory() == null)
         {
            throw new IllegalStateException("No remote interface defined");
         }

         if(id == null)
         {
            throw new IllegalStateException("No entity identity associated with context");
         }

         if(ejbObject == null)
         {   
            ejbObject = (EJBObject) ((EntityContainer)container).getProxyFactory().getEntityEJBObject(id);  
         }

         return ejbObject;
      }

      public EJBLocalObject getEJBLocalObject()
      {
         if(container.getLocalHomeClass()==null)
         {
            throw new IllegalStateException("No local interface for bean");
         }

         if(id == null)
         {
            throw new IllegalStateException("No entity identity associated with context");
         }

         if(ejbLocalObject == null)
         {
            ejbLocalObject = ((EntityContainer)container).getLocalProxyFactory().getEntityEJBLocalObject(id);
         }
         return ejbLocalObject;
      }

      public Object getPrimaryKey()
      {
         if(id == null)
         {
            throw new IllegalStateException("No entity identity associated with context");
         }

         return id;
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         return getContainer().getTimerService(id);
      }
   }
}
