package org.jboss.ejb.plugins.local;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.ejb.EJBObject;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBException;


/** The EJBLocal proxy for a stateless session

 @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 @version $Revision: 1.6 $
 */
class StatelessSessionProxy extends LocalProxy
   implements InvocationHandler
{
   static final long serialVersionUID = 5677941766264344565L;

   StatelessSessionProxy(String jndiName, BaseLocalProxyFactory factory)
   {
      super(jndiName, factory);
   }
   
   protected Object getId()
   {
      return jndiName;
   }
   
   public final Object invoke(final Object proxy, final Method m, Object[] args)
      throws Throwable
   {
      Object retValue = null;
      if (args == null)
         args = EMPTY_ARGS;
      
      // Implement local methods
      if (m.equals(TO_STRING))
      {
         retValue = jndiName + ":Stateless";
      }
      else if (m.equals(EQUALS))
      {
         retValue = invoke(proxy, IS_IDENTICAL, args);
      }
      else if (m.equals(HASH_CODE))
      {
         // We base the stateless hash on the hash of the proxy...
         // MF XXX: it could be that we want to return the hash of the name?
         retValue = new Integer(this.hashCode());
      }
      else if (m.equals(GET_PRIMARY_KEY))
      {
         // The object identifier of a session object is, in general, opaque to the client.
         // The result of getPrimaryKey() on a session EJBObject reference results in java.rmi.RemoteException.
         // The result of getPrimaryKey() on a session EJBLocalObject reference results in javax.ejb.EJBException.
         if (proxy instanceof EJBObject)
         {
            throw new RemoteException("Call to getPrimaryKey not allowed on session bean");
         }
         if (proxy instanceof EJBLocalObject)
         {
            throw new EJBException("Call to getPrimaryKey not allowed on session bean");
         }
      }
      else if (m.equals(GET_EJB_HOME))
      {
         InitialContext ctx = new InitialContext();
         return ctx.lookup(jndiName);
      }
      else if (m.equals(IS_IDENTICAL))
      {
         // All stateless beans are identical within a home,
         // if the names are equal we are equal
         retValue = isIdentical(args[0], jndiName + ":Stateless");
      }
      // If not taken care of, go on and call the container
      else
      {
         retValue = factory.invoke(jndiName, m, args);
      }

      return retValue;
   }
}
