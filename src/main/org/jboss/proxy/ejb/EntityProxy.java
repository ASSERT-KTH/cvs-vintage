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

import java.util.HashMap;
import java.rmi.MarshalledObject;
import java.lang.reflect.Method;

import javax.naming.InitialContext;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

import org.jboss.ejb.CacheKey;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invoker;

import org.jboss.proxy.ejb.handle.EntityHandleImpl;
import org.jboss.util.FinderResults;


/**
* An EJB entity bean proxy class.
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.1 $
*
* <p><b>2001/11/19: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class EntityProxy
extends GenericProxy
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
//   private static final long serialVersionUID = -1523442773137704949L;
   
   // Attributes ----------------------------------------------------
   
   /** The primary key of the entity bean. */
   protected CacheKey cacheKey;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public EntityProxy() {}
   
   /**
   * Construct a <tt>EntityProxy</tt>.
   *
   * @param name          The JNDI name of the container that we proxy for.
   * @param id            The primary key of the entity.
   * @param invoker       An invoker
   *
   * @throws NullPointerException     Id may not be null.
   */
   public EntityProxy(String jndiName, Object id, Invoker invoker)
   {
      super(jndiName, invoker);
      
      if (id == null)
         throw new NullPointerException("Id may not be null");
      
      // make sure that our id is a CacheKey
      if (id instanceof CacheKey) {
         this.cacheKey = (CacheKey)id;
      }
      else {
         // In case we pass the Object or anything else we encapsulate
         cacheKey = new CacheKey(id);
      }
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
      if (args == null)
         args = EMPTY_ARGS;
      
      // Implement local methods
      if (m.equals(TO_STRING)) {
         return jndiName + ":" + cacheKey.getId().toString();
      }
      else if (m.equals(EQUALS)) {
         return invoke(proxy, IS_IDENTICAL, args);
      }
      else if (m.equals(HASH_CODE)) {
         return new Integer(cacheKey.getId().hashCode());
      }
      
      // Implement local EJB calls
      else if (m.equals(GET_HANDLE)) {
         return new EntityHandleImpl(jndiName, cacheKey.getId());
      }
      else if (m.equals(GET_PRIMARY_KEY)) {
         return cacheKey.getId();
      }
      else if (m.equals(GET_EJB_HOME)) {
         return getEJBHome();
      }
      else if (m.equals(IS_IDENTICAL)) {
         return isIdentical(args[0], cacheKey.getId());
      }
      
      // If not taken care of, go on and call the container
      else {
         return invoke(createInvocation(cacheKey, m, args));
      }
   }
   public Invocation createInvocation(CacheKey id, Method m, Object[] args)
   {
      Invocation invocation = new Invocation(new HashMap());
      
      invocation.setContainer(objectName);
      invocation.setType("remote");
      invocation.setId(id);
      invocation.setMethod(m);
      invocation.setArguments(args);    
      
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
      out.writeObject(cacheKey);
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
      cacheKey = (CacheKey)in.readObject();
      
      // Private -------------------------------------------------------
      
      // Inner classes -------------------------------------------------
   }
}
   
   