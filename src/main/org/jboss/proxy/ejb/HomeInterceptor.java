/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb;

import java.lang.reflect.Method;

import org.jboss.invocation.Invocation;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.RemoveException;
import javax.ejb.Handle;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.HomeHandle;

import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;

/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.3 $
*
* <p><b>2001/11/21: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class HomeInterceptor
   extends GenericEJBInterceptor
{
   // Constants -----------------------------------------------------

   /** Serial Version Identifier. */
//   private static final long serialVersionUID = 432426690456622923L;
   
   // Static --------------------------------------------------------
   
   protected static final Object[] EMPTY_ARGS = {};
   
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
   public final static Integer EJB_METADATA = new Integer(new String("EJB_METADATA").hashCode());
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public HomeInterceptor() {}
   
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
   public Object invoke(Invocation invocation)
   throws Throwable
   {
      InvocationContext ctx = invocation.getInvocationContext();

      Method m = invocation.getMethod();

      // Implement local methods
      if (m.equals(TO_STRING)) {
         return ctx.getValue(JNDI_NAME).toString() + "Home";
      }
      else if (m.equals(EQUALS)) {
         // equality of the proxy home is based on names...
         return new Boolean(invocation.getArguments()[0].toString().equals(ctx.getValue(JNDI_NAME).toString() + "Home"));
      }
      else if (m.equals(HASH_CODE)) {
         return new Integer(this.hashCode());
      }
      
      // Implement local EJB calls
      else if (m.equals(GET_HOME_HANDLE)) {
         return new HomeHandleImpl((String)ctx.getValue(JNDI_NAME));
      }
      else if (m.equals(GET_EJB_META_DATA)) {
         return ctx.getValue(EJB_METADATA);
      }
      else if (m.equals(REMOVE_BY_HANDLE)) {
         // First get the EJBObject
         EJBObject object = ((Handle) invocation.getArguments()[0]).getEJBObject();

         // remove the object from here
         object.remove();

         // Return Void
         return Void.TYPE;
      }
      else if (m.equals(REMOVE_BY_PRIMARY_KEY)) {
         // Session beans must throw RemoveException (EJB 1.1, 5.3.2)
         if (((EJBMetaData) ctx.getValue(EJB_METADATA)).isSession())
            throw new RemoveException("Session beans cannot be removed by primary key.");

         // The trick is simple we trick the container in believe it
         // is a remove() on the instance
         Object id = invocation.getArguments()[0];

         // Just override the Invocation going out
         invocation.setId(id);
         invocation.setType(Invocation.REMOTE);
         invocation.setMethod(REMOVE_OBJECT);
         invocation.setArguments(EMPTY_ARGS);
         return getNext().invoke(invocation);
      }

      // If not taken care of, go on and call the container
      else {
         
         invocation.setType(Invocation.HOME);
         // Create an Invocation
         return getNext().invoke(invocation);
     }
   }
}
