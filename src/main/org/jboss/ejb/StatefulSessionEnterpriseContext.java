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
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class StatefulSessionEnterpriseContext
   extends EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EJBObject ejbObject;
   boolean invoked = false;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public StatefulSessionEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
      ((SessionBean)instance).setSessionContext(new StatefulSessionContextImpl());
   }
   
   // Public --------------------------------------------------------
   public void discard()
      throws RemoteException
   {
//      ((SessionBean)instance).unsetEntityContext();
   }
   
   public void setEJBObject(EJBObject eo) { ejbObject = eo; }
   public EJBObject getEJBObject() { return ejbObject; }
   
   public void setInvoked(boolean invoked) { this.invoked = invoked; }
   public boolean isInvoked() { return invoked; }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   protected class StatefulSessionContextImpl
      extends EJBContextImpl
      implements SessionContext
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

