package org.jboss.ejb.plugins.local;

import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

/** The EJBLocal proxy for a stateful session

 @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 @version $Revision: 1.6 $
 */
class StatefulSessionProxy extends LocalProxy
   implements InvocationHandler
{
   static final long serialVersionUID = -3113762511947535929L;
   private Object id;

   StatefulSessionProxy(String jndiName, Object id, BaseLocalProxyFactory factory)
   {
      super(jndiName, factory);
      this.id = id;
   }

   protected Object getId()
   {
      return id;
   }

   public final Object invoke(final Object proxy, final Method m,
      Object[] args)
      throws Throwable
   {
      if (args == null)
         args = EMPTY_ARGS;

      // The object identifier of a session object is, in general, opaque to the client. 
      // The result of getPrimaryKey() on a session EJBObject reference results in java.rmi.RemoteException.
      // The result of getPrimaryKey() on a session EJBLocalObject reference results in javax.ejb.EJBException.
      if (m.equals(GET_PRIMARY_KEY))
      {
         if (proxy instanceof EJBObject)
         {
            throw new RemoteException("Call to getPrimaryKey not allowed on session bean");
         }
         if (proxy instanceof EJBLocalObject)
         {
            throw new EJBException("Call to getPrimaryKey not allowed on session bean");
         }
      }

      Object retValue = super.invoke( proxy, m, args );
      if (retValue == null)
      {
         // If not taken care of, go on and call the container
         retValue = factory.invoke(id, m, args);
      }

      return retValue;
   }
}
