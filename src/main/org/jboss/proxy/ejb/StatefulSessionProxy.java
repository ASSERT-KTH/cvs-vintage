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

import java.lang.reflect.Method;
import java.rmi.MarshalledObject;
import java.util.HashMap;

import javax.naming.InitialContext;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.naming.Name;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;

import org.jboss.proxy.ejb.handle.StatefulHandleImpl;
import org.jboss.util.FinderResults;

/**
* An EJB stateful session bean proxy class.
*   
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.5 $
*
* <p><b>2001/11/23: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class StatefulSessionProxy
extends GenericProxy
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
  // private static final long serialVersionUID = 1379411137308931705L;
   
   // Attributes ----------------------------------------------------
   
   /** JBoss generated identifier. */
   protected Object id;

   // Static --------------------------------------------------------
  
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public StatefulSessionProxy() {}
   
   /**
   * Construct a <tt>StatefulSessionProxy</tt>.
   *
   * @param name          The JNDI name of the container that we proxy for.
   * @param container     The remote interface of the invoker for which
   *                      this is a proxy for.
   * @param id            JBoss generated identifier.
   * @param optimize      True if the proxy will attempt to optimize
   *                      VM-local calls.
   */
   public StatefulSessionProxy(
      int objectName,
      String name,
      Object cacheID,
      Invoker invoker)
   {
      super(objectName,name,invoker);
      id = cacheID;
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
         return jndiName + ":" + id.toString();
      }
      else if (m.equals(EQUALS)) {
         return invoke(proxy, IS_IDENTICAL, args);
      }
      else if (m.equals(HASH_CODE)) {
         return new Integer(id.hashCode());
      }

      // Implement local EJB calls
      else if (m.equals(GET_HANDLE)) {
         return new StatefulHandleImpl(objectName,jndiName,invoker, id);
      }
      else if (m.equals(GET_EJB_HOME)) {

         return getEJBHome();
      }
      else if (m.equals(GET_PRIMARY_KEY)) {

         return id;
      }
      else if (m.equals(IS_IDENTICAL)) {
         // MF FIXME
         // See above, this is not correct but works for now (do jboss1.0 PKHolder hack in here)
         return isIdentical(args[0], id);
      }

      // If not taken care of, go on and call the container
      else {
         return invoke(createInvocation(id, m, args));
      }
   }

   public Invocation createInvocation(Object id, Method m, Object[] args)
     throws Exception
   {
      Invocation invocation = new Invocation(new HashMap());
      invocation.setContainer(new Integer(objectName));
      invocation.setType(Invocation.REMOTE);
      invocation.setId(id);
      invocation.setMethod(m);
      invocation.setArguments(args);
      invocation.setTransaction(getTransaction());

      return invocation;
   
   }
   // Package protected ---------------------------------------------
   
   
   // Protected -----------------------------------------------------
   
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
      out.writeObject(id);
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
      id = in.readObject();
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
