/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class EntityEnterpriseContext
   extends EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EJBObject ejbObject;
   boolean invoked = false;
   boolean synched = false;
   
   Object cacheCtx;
   Object persistenceCtx;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public EntityEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
      ((EntityBean)instance).setEntityContext(new EntityContextImpl());
   }
   
   // Public --------------------------------------------------------
   public void discard()
      throws RemoteException
   {
      ((EntityBean)instance).unsetEntityContext();
   }
   
   public void setEJBObject(EJBObject eo) { ejbObject = eo; }
   public EJBObject getEJBObject() { return ejbObject; }
   
   public void setPersistenceContext(Object ctx) { this.persistenceCtx = ctx; }
   public Object getPersistenceContext() { return persistenceCtx; }
   
   public void setCacheContext(Object ctx) { this.cacheCtx = ctx; }
   public Object getCacheContext() { return cacheCtx; }
   
   public void setInvoked(boolean invoked) { this.invoked = invoked; }
   public boolean isInvoked() { return invoked; }

   public void setSynchronized(boolean synched) { this.synched = synched; }
   public boolean isSynchronized() { return synched; }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   protected class EntityContextImpl
      extends EJBContextImpl
      implements EntityContext
   {
      public EJBObject getEJBObject()
      {
         return ejbObject;
      }
      
      public Object getPrimaryKey()
      {
         return id;
      }
   }
}

