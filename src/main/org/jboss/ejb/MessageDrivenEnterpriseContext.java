/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalStateException;

import java.security.Principal;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import javax.transaction.UserTransaction;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.MessageDrivenContext;
import javax.ejb.MessageDrivenBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;

import org.jboss.logging.Logger;

import org.jboss.metadata.MessageDrivenMetaData;

/**
 * Context for message driven beans, based on Stateless.
 * FIXME - not yet verified agains spec!!!
 *	<description> 
 *      
 *	@see <related>
 *   @author Peter Antman (peter.antman@tim.se)
 *	@author Rickard �berg (rickard.oberg@telkel.com)
 *  @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@version $Revision: 1.2 $
 */
public class MessageDrivenEnterpriseContext
   extends EnterpriseContext
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    //EJBObject ejbObject;
   MessageDrivenContext ctx;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public MessageDrivenEnterpriseContext(Object instance, Container con)
      throws Exception
   {
      super(instance, con);
      ctx = new MessageDrivenContextImpl();
	  
	  ((MessageDrivenBean)instance).setMessageDrivenContext(ctx);
      
      try
      {
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
   }
   
   // Public --------------------------------------------------------
    // FIXME
    // Here we have some problems. If we are to use the Stateless stuff,
    // should we inherit from StatelessSessionEnterpriseContext or what?

    public void setEJBObject(EJBObject eo) { 
	 throw new Error("Not applicatable for MessageDrivenContext");
	//NOOP
	//ejbObject = eo; 
    }
   public EJBObject getEJBObject() { 
       throw new Error("Not applicatable for MessageDrivenContext");
       //return ejbObject; 
   }
    // This is used at least in The pool, created there even!!!
    // and in interceptors, ugh
   public SessionContext getSessionContext() {
	  throw new Error("Not applicatable for MessageDrivenContext");
       //return ctx;
   }

    public MessageDrivenContext getMessageDrivenContext() {
	
	return ctx;
    }
    
   // EnterpriseContext overrides -----------------------------------
   public void discard()
      throws RemoteException
   {
      ((MessageDrivenBean)instance).ejbRemove();
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   protected class MessageDrivenContextImpl
      extends EJBContextImpl
      implements MessageDrivenContext
   {
             public EJBHome getEJBHome() 
      { 
	  Logger.log("MessageDriven bean is not allowed to call getEJBHome");
	  throw new IllegalStateException("Not valid for MessageDriven beans");
      }

       public boolean isCallerInRole(String id) 
       { 
	   Logger.log("MessageDriven bean is not allowed to call isCallerInRole");
	  throw new IllegalStateException("Not valid for MessageDriven beans");
       }


       public Principal getCallerPrincipal() 
       { 
	   Logger.log("MessageDriven bean is not allowed to call getCallerPrincipal()");
	  throw new IllegalStateException("Not valid for MessageDriven beans");
       }

       public boolean getRollbackOnly() 
      { 
	  if (((MessageDrivenMetaData)con.getBeanMetaData()).getAcknowledgeMode() != MessageDrivenMetaData.CLIENT_ACKNOWLEDGE_MODE) {
	      // NO transaction
	      Logger.log("MessageDriven bean is not allowed to call getRollbackOnly with this transaction settings");
	  throw new IllegalStateException("MessageDriven bean is not allowed to call getRollbackOnly with this transaction settings");
	  } else {
	      return super.getRollbackOnly();
	  }
      }
       
       public void setRollbackOnly() 
       { 
	   if (((MessageDrivenMetaData)con.getBeanMetaData()).getAcknowledgeMode() != MessageDrivenMetaData.CLIENT_ACKNOWLEDGE_MODE) {
	       // NO transaction
	       Logger.log("MessageDriven bean is not allowed to call setRollbackOnly with this transaction settings");
	       throw new IllegalStateException("MessageDriven bean is not allowed to call setRollbackOnly with this transaction settings");
	   }else {
	      super.setRollbackOnly();
	      
	   }
       }
       
       public UserTransaction getUserTransaction() 
       { 
	   if (((MessageDrivenMetaData)con.getBeanMetaData()).isContainerManagedTx() ) {
	       // NO transaction
	       Logger.log("MessageDriven bean is not allowed to get a UserTransactio: transaction is containeremanaged");
	       throw new IllegalStateException("MessageDriven bean is not allowed to get a UserTransactio: transaction is containeremanaged");
	   }else {
	       return super.getUserTransaction();
	       
	   }  
       }
   }
}
