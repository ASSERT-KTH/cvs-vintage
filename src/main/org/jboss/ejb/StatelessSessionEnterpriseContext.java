/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.transaction.UserTransaction;

/**
 * The enterprise context for stateless session beans.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.19 $
 */
public class StatelessSessionEnterpriseContext extends EnterpriseContext
{
   private EJBObject ejbObject;
   private EJBLocalObject ejbLocalObject;
   private SessionContext ctx;

   public StatelessSessionEnterpriseContext(Object instance, Container container)
      throws Exception
   {
      super(instance, container);
      ctx = new SessionContextImpl();

      ((SessionBean)instance).setSessionContext(ctx);

      try
      {
         Method ejbCreate = instance.getClass().getMethod("ejbCreate", new Class[0]);
         ejbCreate.invoke(instance, new Object[0]);
      }
      catch(InvocationTargetException e)
      {
         Throwable ex = e.getTargetException();
         if(ex instanceof EJBException)
         {
            throw (Exception)ex;
         }
         else if(ex instanceof RuntimeException)
         {
            // Transform runtime exception into what a bean *should* have thrown
            // Dain: I don't think this is legal
            throw new EJBException((Exception)ex);
         }
         else if(ex instanceof Exception)
         {
            throw (Exception)ex;
         }
         else
         {
            throw (Error)ex;
         }
      }
   }

   public void setEJBObject(EJBObject eo)
   {
      ejbObject = eo;
   }

   public EJBObject getEJBObject()
   {
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

   public SessionContext getSessionContext()
   {
      return ctx;
   }

   public void discard() throws RemoteException
   {
      ((SessionBean)instance).ejbRemove();
   }

   public EJBContext getEJBContext()
   {
      return ctx;
   }

   protected class SessionContextImpl
      extends EJBContextImpl
      implements SessionContext
   {
      public EJBObject getEJBObject()
      {
         if(((StatelessSessionContainer)container).getProxyFactory()==null)
         {
            throw new IllegalStateException( "No remote interface defined." );
         }

         if(ejbObject == null)
         {
               ejbObject = (EJBObject) ((StatelessSessionContainer)container).getProxyFactory().getStatelessSessionEJBObject();

         }

         return ejbObject;
      }

      public EJBLocalObject getEJBLocalObject()
      {
         if(container.getLocalHomeClass()==null)
         {
            throw new IllegalStateException( "No local interface for bean." );
         }

         if(ejbLocalObject == null)
         {
            ejbLocalObject = ((StatelessSessionContainer)container).getLocalProxyFactory().getStatelessSessionEJBLocalObject();
         }
         return ejbLocalObject;
      }
   }
}
