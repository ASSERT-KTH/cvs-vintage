/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.jboss.invocation.Invoker;
import org.jboss.invocation.Invocation;
import org.jboss.proxy.ejb.handle.StatelessHandleImpl;


/**
* An EJB stateless session bean proxy class.
*   
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.4 $
*
* <p><b>2001/11/23: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>  
*/
public class StatelessSessionProxy
extends GenericProxy
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   //   private static final long serialVersionUID = 2327647224051998978L;
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public StatelessSessionProxy() {}
   
   /**
   * Construct a <tt>StatelessSessionProxy</tt>.
   *
   */
   public StatelessSessionProxy(int objectName,
      String jndiName,
      Invoker invoker)
   {
      super(objectName, jndiName,invoker);
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
   public final Object invoke(final Object proxy,
      final Method m,
      Object[] args)
   throws Throwable
   {
      // Normalize args to always be an array
      if (args == null)
         args = EMPTY_ARGS;
      
      // Implement local methods
      if (m.equals(TO_STRING)) {
         return jndiName + ":Stateless";
      }
      else if (m.equals(EQUALS)) {
         return invoke(proxy, IS_IDENTICAL, args);
      }
      else if (m.equals(HASH_CODE)) {
         // We base the stateless hash on the hash of the proxy...
         // MF XXX: it could be that we want to return the hash of the name?
         return new Integer(this.hashCode());
      }
      
      // Implement local EJB calls
      else if (m.equals(GET_HANDLE)) {
         return new StatelessHandleImpl(jndiName);
      }
      else if (m.equals(GET_PRIMARY_KEY)) {
         
         return jndiName;
      }
      else if (m.equals(GET_EJB_HOME)) {
         return getEJBHome();
      }
      
      else if (m.equals(IS_IDENTICAL)) {
         // All stateless beans are identical within a home,
         // if the names are equal we are equal
         return isIdentical(args[0], jndiName);
      }
      
      // If not taken care of, go on and call the container
      else {
         return invoke(createInvocation(m, args));
      }
   }
   
   public Invocation createInvocation(Method m, Object[] args)
   throws Exception
   {
      Invocation invocation = new Invocation(new HashMap());
      
      invocation.setContainer(new Integer(objectName));
      invocation.setType(Invocation.REMOTE);
      invocation.setMethod(m);
      invocation.setArguments(args);
      invocation.setTransaction(getTransaction());
      
      return invocation;
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
