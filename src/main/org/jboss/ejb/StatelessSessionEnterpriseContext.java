/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.SessionBean;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.3 $
 */
public class StatelessSessionEnterpriseContext
   extends EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EJBObject ejbObject;
   SessionContext ctx;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public StatelessSessionEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
      ctx = new SessionContextImpl();
	  
	  ((SessionBean)instance).setSessionContext(ctx);
      
      try
      {
         Method ejbCreate = instance.getClass().getMethod("ejbCreate", new Class[0]);
         ejbCreate.invoke(instance, new Object[0]);
      } catch (InvocationTargetException e)
      {
         throw new ServerException("Could not call ejbCreate", (Exception)e.getTargetException());
      } catch (Exception e)
      {
         throw new ServerException("Could not call ejbCreate", e);
      }
   }
   
   // Public --------------------------------------------------------
   public void setEJBObject(EJBObject eo) { ejbObject = eo; }
   public EJBObject getEJBObject() { return ejbObject; }
   
   public SessionContext getSessionContext() {
	   return ctx;
   }

   // EnterpriseContext overrides -----------------------------------
   public void discard()
      throws RemoteException
   {
      ((SessionBean)instance).ejbRemove();
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   protected class SessionContextImpl
      extends EJBContextImpl
      implements SessionContext
   {
      public EJBObject getEJBObject()
      {
		  if (ejbObject == null) {
			  
			  	try {
					
			        ejbObject = ((StatelessSessionContainer)con).getContainerInvoker().getStatelessSessionEJBObject(); 
				}
			 	catch (RemoteException re) {
					// ...
					throw new IllegalStateException();
				}
			} 	
    
	     	return ejbObject;
      }
   }
}

