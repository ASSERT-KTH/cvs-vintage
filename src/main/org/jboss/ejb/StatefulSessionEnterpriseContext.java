/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.5 $
 */
public class StatefulSessionEnterpriseContext
   extends EnterpriseContext
	implements java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EJBObject ejbObject;
   Object cacheCtx;
   Object persistenceCtx;
	
	SessionContext ctx;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public StatefulSessionEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
		ctx = new StatefulSessionContextImpl();
      ((SessionBean)instance).setSessionContext(ctx);
   }
   
   // Public --------------------------------------------------------
   public void discard()
      throws RemoteException
   {
		// Do nothing
   }
   
   // During activation of stateful session beans we replace the instance by the one read from the file
   public void setInstance(Object instance) 
    { 
       this.instance = instance; 
    }
   
   public void setEJBObject(EJBObject eo) { ejbObject = eo; }
   public EJBObject getEJBObject() { return ejbObject; }
   
   public void setPersistenceContext(Object ctx) { this.persistenceCtx = ctx; }
   public Object getPersistenceContext() { return persistenceCtx; }
   
   public void setCacheContext(Object ctx) { this.cacheCtx = ctx; }
   public Object getCacheContext() { return cacheCtx; }
	
	public SessionContext getSessionContext()
	{
		return ctx;
	}

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
   private void writeObject(java.io.ObjectOutputStream out)
      throws IOException, ClassNotFoundException
   {
		// No state
   }
	
   private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
		// No state
   }

   // Inner classes -------------------------------------------------
   protected class StatefulSessionContextImpl
      extends EJBContextImpl
      implements SessionContext
   {
		public EJBObject getEJBObject()
		{
			if (ejbObject == null) {
				
				try {
					
					ejbObject = ((StatefulSessionContainer)con).getContainerInvoker().getStatefulSessionEJBObject(id); 
				}
				catch (RemoteException re) {
					// ...
					throw new IllegalStateException();
				}
			} 	
			
			return ejbObject;
      	}
      
      	public Object getPrimaryKey()
      	{
         return id;
      	}
   	}
}

