/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.local;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import javax.ejb.EJBLocalObject;

/** Abstract superclass of local interface proxies.

 @author  <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 @version $Revision: 1.6 $
 */
public abstract class LocalProxy implements Serializable
{
   // Constants -----------------------------------------------------
   static final long serialVersionUID = 8387750757101826407L;
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   /** An empty method parameter list. */
   protected static final Object[] EMPTY_ARGS = {};

   /** {@link Object#toString} method reference. */
   protected static final Method TO_STRING;
   
   /** {@link Object#hashCode} method reference. */
   protected static final Method HASH_CODE;
   
   /** {@link Object#equals} method reference. */
   protected static final Method EQUALS;
   
   /** {@link EJBObject#getPrimaryKey} method reference. */
   protected static final Method GET_PRIMARY_KEY;
   
   /** {@link EJBObject#getEJBHome} method reference. */
   protected static final Method GET_EJB_HOME;
   
   /** {@link EJBObject#isIdentical} method reference. */
   protected static final Method IS_IDENTICAL;
   
   protected String jndiName;
   protected transient BaseLocalContainerInvoker invoker;

   /**
    * Initialize {@link EJBObject} method references.
    */
   static
   {
      try
      {
         final Class[] empty = {};
         final Class type = EJBLocalObject.class;
         
         GET_PRIMARY_KEY = type.getMethod("getPrimaryKey", empty);
         GET_EJB_HOME = type.getMethod("getEJBLocalHome", empty);
         IS_IDENTICAL = type.getMethod("isIdentical", new Class[] { type });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   /**
    * Initialize {@link Object} method references.
    */
   static
   {
      try
      {
         final Class[] empty = {};
         final Class type = Object.class;
         
         TO_STRING = type.getMethod("toString", empty);
         HASH_CODE = type.getMethod("hashCode", empty);
         EQUALS = type.getMethod("equals", new Class[] { type });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   protected String getJndiName()
   {
      return jndiName;
   }
   protected abstract Object getId();
   
   public LocalProxy(String jndiName, BaseLocalContainerInvoker invoker)
   {
      this.jndiName = jndiName;
      this.invoker = invoker;
   }

   /**
    * Test the identitiy of an <tt>EJBObject</tt>.
    *
    * @param a    <tt>EJBObject</tt>.
    * @param b    Object to test identity with.
    * @return     True if objects are identical.
    *
    * @throws ClassCastException   Not an EJBObject instance.
    */
   Boolean isIdentical(final Object a, final Object b)
   {
      final EJBLocalObject ejb = (EJBLocalObject)a;
      final Object pk = ejb.getPrimaryKey();
      return new Boolean(pk.equals(b));
   }
   
   public Object invoke(final Object proxy, final Method m, Object[] args)
      throws Throwable
   {
      Object id = getId();
      Object retValue = null;

      // Implement local methods
      if (m.equals(TO_STRING))
      {
         retValue = jndiName + ":" + id.toString();
      }
      else if (m.equals(EQUALS))
      {
         retValue = invoke(proxy, IS_IDENTICAL, args );
      }
      else if (m.equals(HASH_CODE))
      {
         retValue = new Integer(id.hashCode());
      }
      
      // Implement local EJB calls
      else if (m.equals(GET_PRIMARY_KEY))
      {
         retValue = id;
      }
      else if (m.equals(GET_EJB_HOME))
      {
         throw new UnsupportedOperationException();
      }
      else if (m.equals(IS_IDENTICAL))
      {
         retValue = isIdentical(args[0], id);
      }
      return retValue;
   }

   /** Restore the jndiName using default serialization and then lookup
    the BaseLocalContainerInvoker using the jndiName
    */
   private void readObject(ObjectInputStream in)
     throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      invoker = (BaseLocalContainerInvoker) BaseLocalContainerInvoker.invokerMap.get(jndiName);
   }

   private void writeObject(ObjectOutputStream out)
      throws IOException
   {
      out.defaultWriteObject();
   }

}
