/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.local;


import java.lang.reflect.Method;


import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;




/**
 * Abstract superclass of local client-side proxies.
 * 
 * @author  <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 */
public abstract class LocalProxy
{
   // Constants -----------------------------------------------------

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
    
    protected abstract String getJndiName();
    protected abstract Object getId();

    /**
     * Initialize {@link EJBObject} method references.
     */
    static {
        try {
            final Class[] empty = {};
            final Class type = EJBLocalObject.class;
         
			GET_PRIMARY_KEY = type.getMethod("getPrimaryKey", empty);
			GET_EJB_HOME = type.getMethod("getEJBLocalHome", empty);
			IS_IDENTICAL = type.getMethod("isIdentical", new Class[] { type });
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }


   /**
    * Initialize {@link Object} method references.
    */
   static {
       try {
           final Class[] empty = {};
           final Class type = Object.class;

           TO_STRING = type.getMethod("toString", empty);
           HASH_CODE = type.getMethod("hashCode", empty);
           EQUALS = type.getMethod("equals", new Class[] { type });
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new ExceptionInInitializerError(e);
       }
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

      public Object invoke(final Object proxy,
                               final Method m,
                               Object[] args)
        throws Throwable
       {
          Object id = getId();
          String jndiName = getJndiName();
          
          // Implement local methods
          if (m.equals(TO_STRING)) {
              return jndiName + ":" + id.toString();
          }
          else if (m.equals(EQUALS)) {
              return invoke(proxy, IS_IDENTICAL, args );
          }
          else if (m.equals(HASH_CODE)) {
            return new Integer(id.hashCode());
          }

          // Implement local EJB calls
          else if (m.equals(GET_PRIMARY_KEY)) {
              return id;
          }
          else if (m.equals(GET_EJB_HOME)) {
             throw new UnsupportedOperationException(); 
             //return getEJBHome();
          }
          else if (m.equals(IS_IDENTICAL)) {
              return isIdentical(args[0], id);
          }
          return null;
      }

}

