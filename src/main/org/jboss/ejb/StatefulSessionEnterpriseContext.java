/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.rmi.RemoteException;
import java.security.Principal;

import javax.ejb.EJBLocalObject;
import javax.ejb.*;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;


/**
 * The enterprise context for stateful session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 * @version $Revision: 1.27 $
 */
public class StatefulSessionEnterpriseContext
   extends EnterpriseContext
   implements Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private EJBObject ejbObject;
   private EJBLocalObject ejbLocalObject;
   private SessionContext ctx;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public StatefulSessionEnterpriseContext(Object instance, Container con)
      throws RemoteException
   {
      super(instance, con);
      ctx = new StatefulSessionContextImpl();
      try
      {
         pushInMethodFlag(IN_SET_SESSION_CONTEXT);
         ((SessionBean)instance).setSessionContext(ctx);
      }
      finally
      {
         popInMethodFlag();
      }
   }

   // Public --------------------------------------------------------

   public void discard() throws RemoteException
   {
      // Do nothing
   }

   public EJBContext getEJBContext()
   {
      return ctx;
   }

   /**
    * During activation of stateful session beans we replace the instance
    * by the one read from the file.
    */
   public void setInstance(Object instance)
   {
      this.instance = instance;
      try
      {
         ((SessionBean)instance).setSessionContext(ctx);
      }
      catch (Exception x)
      {
         log.error("Failed to setSessionContext", x);
      }
   }

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
    
   public SessionContext getSessionContext()
   {
      return ctx;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private void writeObject(ObjectOutputStream out)
      throws IOException, ClassNotFoundException
   {
      // No state
   }
    
   private void readObject(ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      // No state
   }

   // Inner classes -------------------------------------------------

   protected class StatefulSessionContextImpl
      extends EJBContextImpl
      implements SessionContext
   {

      public EJBHome getEJBHome()
      {
         assertAllowedIn("getEJBHome",
                 IN_SET_SESSION_CONTEXT |
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.getEJBHome();
      }

      public EJBLocalHome getEJBLocalHome()
      {
         assertAllowedIn("getEJBLocalHome",
                 IN_SET_SESSION_CONTEXT |
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.getEJBLocalHome();
      }

      /** Get the Principal for the current caller. This method
       cannot return null according to the ejb-spec.
       */
      public Principal getCallerPrincipal()
      {
         assertAllowedIn("getCallerPrincipal",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.getCallerPrincipal();
      }

      public boolean isCallerInRole(String id)
      {
         assertAllowedIn("isCallerInRole",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         return super.isCallerInRole(id);
      }

      public EJBObject getEJBObject()
      {
         assertAllowedIn("getEJBObject",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         if (((StatefulSessionContainer)con).getProxyFactory()==null)
            throw new IllegalStateException( "No remote interface defined." );

         if (ejbObject == null) {
               ejbObject = (EJBObject) ((StatefulSessionContainer)con).getProxyFactory().getStatefulSessionEJBObject(id);
         }  

         return ejbObject;
      }

      public EJBLocalObject getEJBLocalObject()
      {
         assertAllowedIn("getEJBLocalObject",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD |
                 IN_AFTER_BEGIN | IN_BEFORE_COMPLETION | IN_AFTER_COMPLETION);

         if (con.getLocalHomeClass()==null)
            throw new IllegalStateException( "No local interface for bean." );
         if (ejbLocalObject == null)
         {
            ejbLocalObject = ((StatefulSessionContainer)con).getLocalProxyFactory().getStatefulSessionEJBLocalObject(id);
         }
         return ejbLocalObject;
      }

      public boolean getRollbackOnly()
      {
         assertAllowedIn("getRollbackOnly",
                 IN_BUSINESS_METHOD | IN_AFTER_BEGIN | IN_BEFORE_COMPLETION);

         return super.getRollbackOnly();
      }

      public void setRollbackOnly()
      {
         assertAllowedIn("setRollbackOnly",
                 IN_BUSINESS_METHOD | IN_AFTER_BEGIN | IN_BEFORE_COMPLETION);

         super.setRollbackOnly();
      }

      public UserTransaction getUserTransaction()
      {
         assertAllowedIn("getUserTransaction",
                 IN_EJB_CREATE | IN_EJB_REMOVE | IN_EJB_ACTIVATE | IN_EJB_PASSIVATE | IN_BUSINESS_METHOD);

         return super.getUserTransaction();
      }

      public TimerService getTimerService() throws IllegalStateException
      {
         throw new IllegalStateException("getTimerService should not be access from a stateful session bean");
      }

      public MessageContext getMessageContext() throws IllegalStateException
      {
         return null;
      }

      public Object getPrimaryKey()
      {
         return id;
      }
   }
}
