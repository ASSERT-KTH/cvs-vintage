/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * An abstract base proxy class from which all bean proxys extend from.
 *
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.5 $
 */
public abstract class BeanProxy
   extends GenericProxy
{
   // Constants -----------------------------------------------------

   /** Serial Version Identifier. */
   private static final long serialVersionUID = -4177999312297604904L;
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   /** {@link EJBObject#getPrimaryKey} method reference. */
   protected static final Method GET_PRIMARY_KEY;

   /** {@link EJBObject#getHandle} method reference. */
   protected static final Method GET_HANDLE;

   /** {@link EJBObject#getEJBHome} method reference. */
   protected static final Method GET_EJB_HOME;

   /** {@link EJBObject#isIdentical} method reference. */
   protected static final Method IS_IDENTICAL;

   /**
    * Initialize {@link EJBObject} method references.
    */
   static
   {
      try
      {
         final Class[] empty = {};
         final Class type = EJBObject.class;
         
         GET_PRIMARY_KEY = type.getMethod("getPrimaryKey", empty);
         GET_HANDLE = type.getMethod("getHandle", empty);
         GET_EJB_HOME = type.getMethod("getEJBHome", empty);
         IS_IDENTICAL = type.getMethod("isIdentical", new Class[] { type });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   // Constructors --------------------------------------------------

   /**
    * No-argument constructor for externalization.
    */
   public BeanProxy() {}

   /**
    * Initialze.
    *
    * @param name          The JNDI name of the container that we proxy for.
    * @param container     The remote interface of the invoker for which
    *                      this is a proxy for.
    * @param optimize      True if the proxy will attempt to optimize
    *                      VM-local calls.
    */
   protected BeanProxy(final String name,
                       final ContainerRemote container,
                       final boolean optimize)
   {
      super(name, container, optimize);
   }

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   
   /**
    * Get a <tt>EJBHome</tt> reference for this proxy.
    * 
    * @return  <tt>EJBHome</tt> reference.
    *
    * @throws NamingException    Failed to create InitalContext or lookup
    *                            EJBHome reference.
    */
   protected EJBHome getEJBHome() throws NamingException
   {
      // get a reference to the correct context
      final InitialContext ctx = createInitialContext();

      try
      {
         return (EJBHome)ctx.lookup(name);
      }
      finally
      {
         ctx.close();
      }
   }

   /**
    * Test the identitiy of an <tt>EJBObject</tt>.
    *
    * @param a    <tt>EJBObject</tt>.
    * @param b    Object to test identity with.
    * @return     True if objects are identical.
    *
    * @throws RemoteException      Failed to get primary key.
    * @throws ClassCastException   Not an EJBObject instance.
    */
   protected Boolean isIdentical(final Object a, final Object b)
      throws RemoteException
   {
      if( a == null )
         return false;

      final EJBObject ejb = (EJBObject)a;
      final Object pk = ejb.getPrimaryKey();
      return new Boolean(pk.equals(b));
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
