/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.security.Principal;

import java.rmi.RemoteException;

import javax.ejb.EJBContext;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.MessageDrivenContext;
import javax.ejb.MessageDrivenBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;

import org.jboss.metadata.MetaData;
import org.jboss.metadata.MessageDrivenMetaData;

/**
 * Context for message driven beans, based on Stateless.
 * FIXME - not yet verified agains spec!!!
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.14 $
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
         Method ejbCreate = instance.getClass().getMethod("ejbCreate",
                                                          new Class[0]);
         ejbCreate.invoke(instance, new Object[0]);
      } catch (InvocationTargetException e)
      {
         Throwable ex = e.getTargetException();
         if (ex instanceof EJBException)
            throw (Exception)ex;
         else if (ex instanceof RuntimeException)
            // Transform runtime exception into what a bean *should*
            // have thrown
            throw new EJBException((Exception)ex);
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

   public void discard() throws RemoteException
   {
      ((MessageDrivenBean)instance).ejbRemove();
   }

   public EJBContext getEJBContext()
   {
      return ctx;
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
         log.info("MessageDriven bean is not allowed to call getEJBHome");
         throw new IllegalStateException("Not valid for MessageDriven beans");
      }

      public boolean isCallerInRole(String id)
      {
         log.info("MessageDriven bean is not allowed to call isCallerInRole");
         throw new IllegalStateException("Not valid for MessageDriven beans");
      }


      public Principal getCallerPrincipal()
      {
         log.info("MessageDriven bean is not allowed to call getCallerPrincipal()");
         throw new IllegalStateException("Not valid for MessageDriven beans");
      }

      public boolean getRollbackOnly()
      {
         if (((MessageDrivenMetaData)con.getBeanMetaData()).getMethodTransactionType() != MetaData.TX_REQUIRED) {
            // NO transaction
            log.info("MessageDriven bean is not allowed to call getRollbackOnly with this transaction settings");
            throw new IllegalStateException("MessageDriven bean is not allowed to call getRollbackOnly with this transaction settings");
         } else {
            return super.getRollbackOnly();
         }
      }

      public void setRollbackOnly()
      {
         if (((MessageDrivenMetaData)con.getBeanMetaData()).getMethodTransactionType() != MetaData.TX_REQUIRED) {
            // NO transaction
            log.info("MessageDriven bean is not allowed to call setRollbackOnly with this transaction settings");
            throw new IllegalStateException("MessageDriven bean is not allowed to call setRollbackOnly with this transaction settings");
         } else {
            super.setRollbackOnly();
         }
      }
   }
}
