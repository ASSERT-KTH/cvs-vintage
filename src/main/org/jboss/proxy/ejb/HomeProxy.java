/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.rmi.MarshalledObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.RemoveException;
import javax.ejb.Handle;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.HomeHandle;

import org.jboss.ejb.CacheKey;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.invocation.Invocation;

import org.jboss.util.FinderResults;

/*
import javax.naming.Name;
import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;
*/
/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.4 $
*
* <p><b>2001/11/21: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class HomeProxy
   extends GenericProxy
{
   // Constants -----------------------------------------------------

   /** Serial Version Identifier. */
//   private static final long serialVersionUID = 432426690456622923L;
   
   // Static --------------------------------------------------------
   
   /** {@link EJBHome#getEJBMetaData} method reference. */
   protected static final Method GET_EJB_META_DATA;
   
   /** {@link EJBHome#getHomeHandle} method reference. */
   protected static final Method GET_HOME_HANDLE;
   
   /** {@link EJBHome#remove(Handle)} method reference. */
   protected static final Method REMOVE_BY_HANDLE;
   
   /** {@link EJBHome#remove(Object)} method reference. */
   protected static final Method REMOVE_BY_PRIMARY_KEY;
   
   /** {@link EJBObject#remove} method reference. */
   protected static final Method REMOVE_OBJECT;
   
   static {
      try {
         final Class empty[] = {};
         final Class type = EJBHome.class;
         
         GET_EJB_META_DATA = type.getMethod("getEJBMetaData", empty);
         GET_HOME_HANDLE = type.getMethod("getHomeHandle", empty);
         REMOVE_BY_HANDLE = type.getMethod("remove", new Class[] { 
               Handle.class 
            });
         REMOVE_BY_PRIMARY_KEY = type.getMethod("remove", new Class[] { 
               Object.class 
            });
         
         // Get the "remove" method from the EJBObject
         REMOVE_OBJECT = EJBObject.class.getMethod("remove", empty);
      }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);            
      }
   }
   
   // Attributes ----------------------------------------------------
   
   /** The EJB meta-data for the {@link EJBHome} reference. */    
   protected EJBMetaData ejbMetaData;
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public HomeProxy() {}
   
   /**
   * Construct a <tt>HomeProxy</tt>.
   *
   */
   public HomeProxy(String jndiName, Invoker invoker, EJBMetaData ejbMetaData)
   {
      super(jndiName, invoker);
      this.ejbMetaData = ejbMetaData;  
   }
   
   // Public --------------------------------------------------------
   
   /**
   * InvocationHandler implementation.
   *
   * @param proxy   The proxy object.
   * @param m       The method being invoked.
   * @param args    The arguments for the method.
   *
   * @throws Throwable    Any exception or error thrown while processing.
   */
   public Object invoke(final Object proxy,
      final Method m,
      Object[] args)
   throws Throwable
   {
      // Normalize args to always be an array
      // Isn't this a bug in the proxy call??
      if (args == null)
         args = EMPTY_ARGS;
      
      // Implement local methods
      if (m.equals(TO_STRING)) {
         return jndiName.toString() + "Home";
      }
      else if (m.equals(EQUALS)) {
         // equality of the proxy home is based on names...
         Object temp = invoke(proxy, TO_STRING, args);
         return new Boolean(temp.equals(jndiName.toString() + "Home"));
      }
      else if (m.equals(HASH_CODE)) {
         return new Integer(this.hashCode());
      }
      
      // Implement local EJB calls
      else if (m.equals(GET_HOME_HANDLE)) {
         return new HomeHandleImpl(jndiName);
      }
      else if (m.equals(GET_EJB_META_DATA)) {
         return ejbMetaData;
      }
      else if (m.equals(REMOVE_BY_HANDLE)) {
         // First get the EJBObject
         EJBObject object = ((Handle) args[0]).getEJBObject();

         // remove the object from here
         object.remove();

         // Return Void
         return Void.TYPE;
      }
      else if (m.equals(REMOVE_BY_PRIMARY_KEY)) {
         // Session beans must throw RemoveException (EJB 1.1, 5.3.2)
         if (ejbMetaData.isSession())
            throw new RemoveException("Session beans cannot be removed by primary key.");

         // The trick is simple we trick the container in believe it
         // is a remove() on the instance
         Object id = new CacheKey(args[0]);

         // create an invocation for the new format

         Invocation invocation = new Invocation(new HashMap());

         invocation.setContainer(objectName);
         invocation.setId(id);
         invocation.setType("remote");
         invocation.setMethod(REMOVE_OBJECT);
         invocation.setArguments(EMPTY_ARGS);
         invocation.setTransaction(getTransaction());
         return invoke(invocation);
      }

      // If not taken care of, go on and call the container
      else {
         // Create an Invocation
         return invoke(createInvocation(m, args));
      }
   }

   public Invocation createInvocation(Method m, Object[] arguments)
     throws Exception
   {
      Invocation invocation = new Invocation(new HashMap());

      invocation.setContainer(objectName);
      invocation.setType("home");
      invocation.setMethod(m);
      invocation.setArguments(arguments);
      invocation.setTransaction(getTransaction());

      return invocation;
   }

   /**
   * Externalization support.
   *
   * @param out
   *
   * @throws IOException
   */
   public void writeExternal(final ObjectOutput out)
   throws IOException
   {
      super.writeExternal(out);
      out.writeObject(ejbMetaData);
   }
   
   /**
   * Externalization support.
   *
   * @param in
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
   public void readExternal(final ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      ejbMetaData = (EJBMetaData)in.readObject();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
