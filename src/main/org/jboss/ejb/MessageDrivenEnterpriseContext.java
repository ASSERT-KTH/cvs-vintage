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
 * Context for message driven beans.
 * 
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.15 $
 */
public class MessageDrivenEnterpriseContext
   extends EnterpriseContext
{
   private MessageDrivenContext ctx;

   /**
    * Construct a <tt>MessageDrivenEnterpriseContext</tt>.
    *
    * <p>Sets the MDB context and calls ejbCreate().
    *
    * @param instance   An instance of MessageDrivenBean
    * @param con        The container for this MDB.
    *
    * @throws Exception    EJBException, Error or Exception.  If RuntimeException
    *                      was thrown by ejbCreate it will be turned into an
    *                      EJBException.
    */
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
      }
      catch (InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         
         if (t instanceof RuntimeException) {
            if (t instanceof EJBException) {
               throw (EJBException)t;
            }
            else {
               // Transform runtime exception into what a bean *should* have thrown
               throw new EJBException((RuntimeException)t);
            }
         }
         else if (t instanceof Exception) {
            throw (Exception)t;
         }
         else if (t instanceof Error) {
            throw (Error)t;
         }
         else {
            throw new org.jboss.util.NestedError("Unexpected Throwable", t);
         }
      }
   }

   public MessageDrivenContext getMessageDrivenContext()
   {
      return ctx;
   }

   // EnterpriseContext overrides -----------------------------------

   /**
    * Calls ejbRemove() on the MDB instance.
    */
   public void discard() throws RemoteException
   {
      ((MessageDrivenBean)instance).ejbRemove();
   }

   public EJBContext getEJBContext()
   {
      return ctx;
   }

   /**
    * The EJBContext for MDBs.
    */
   protected class MessageDrivenContextImpl
      extends EJBContextImpl
      implements MessageDrivenContext
   {
      /**
       * Not allowed for MDB.
       *
       * @throws IllegalStateException
       */
      public EJBHome getEJBHome()
      {
         throw new IllegalStateException("getEJBHome() is not valid for MDB");
      }

      /**
       * Not allowed for MDB.
       *
       * @throws IllegalStateException
       */
      public boolean isCallerInRole(String id)
      {
         throw new IllegalStateException("isCallerInRole(String) is not valid for MDB");
      }

      /**
       * Not allowed for MDB.
       *
       * @throws IllegalStateException
       */
      public Principal getCallerPrincipal()
      {
         throw new IllegalStateException("getCallerPrincipal() is not valid for MDB");
      }

      /** Helper to check if the tx type is TX_REQUIRED. */
      private boolean isTxRequired() {
         MessageDrivenMetaData md = (MessageDrivenMetaData)con.getBeanMetaData();
         return md.getMethodTransactionType() == MetaData.TX_REQUIRED;
      }
      
      /**
       * If transaction type is not REQUIRED then throw an exception.
       *
       * @throws IllegalStateException   If transaction type is not REQUIRED.
       */
      public boolean getRollbackOnly()
      {
         if (!isTxRequired())
         {
            throw new IllegalStateException
               ("MDB is not allowed to call getRollbackOnly unless tx type is REQUIRED");
         }
         else {
            return super.getRollbackOnly();
         }
      }

      /**
       * If transaction type is not REQUIRED then throw an exception.
       *
       * @throws IllegalStateException   If transaction type is not REQUIRED.
       */
      public void setRollbackOnly()
      {
         if (!isTxRequired())
         {
            throw new IllegalStateException
               ("MDB is not allowed to call setRollbackOnly unless tx type is REQUIRED");
         }
         else {
            super.setRollbackOnly();
         }
      }
   }
}
